package be.xplore.githubmetrics.prometheusexporter.pullrequest;

import be.xplore.githubmetrics.domain.pullrequest.GetAllPullRequestsUseCase;
import be.xplore.githubmetrics.domain.pullrequest.PullRequest;
import be.xplore.githubmetrics.domain.pullrequest.PullRequestState;
import be.xplore.githubmetrics.prometheusexporter.ScheduledExporter;
import be.xplore.githubmetrics.prometheusexporter.StartupExporter;
import be.xplore.githubmetrics.prometheusexporter.config.SchedulingProperties;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Service
public class PullRequestExporter implements ScheduledExporter, StartupExporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(PullRequestExporter.class);
    private static final List<Integer> STATE_COUNT_PERIODS = List.of(1, 2, 7, 28, 365);
    private static final List<Integer> THROUGHPUT_PERIODS = List.of(7, 14, 28, 365);
    private final GetAllPullRequestsUseCase getAllPullRequestsUseCase;
    private final MeterRegistry registry;
    private final Map<Integer, Map<PullRequestState, AtomicInteger>> stateCountGauges = new HashMap<>();
    private final Map<Integer, AtomicInteger> throughputGauges = new HashMap<>();

    private final String cronExpression;

    public PullRequestExporter(
            GetAllPullRequestsUseCase getAllPullRequestsUseCase,
            SchedulingProperties schedulingProperties,
            MeterRegistry registry
    ) {
        this.getAllPullRequestsUseCase = getAllPullRequestsUseCase;
        this.registry = registry;
        this.cronExpression = schedulingProperties.pullRequestsInterval();

        this.initPullRequestGauges();

    }

    private static String getThroughputMetricName(int period) {
        return MessageFormat.format(
                "pull_request_throughput_of_last_{0}_days",
                period
        );
    }

    private static String getStateCountMetricName(int period) {
        return MessageFormat.format(
                "pull_requests_count_of_last_{0}_days",
                period
        );
    }

    private void retrieveAndExportPullRequestsMetrics() {
        LOGGER.info("PullRequests scheduled task is running.");
        List<PullRequest> pullRequests = getAllPullRequestsUseCase.getAllPullRequests();

        this.generateAndPublishPullRequestCountsMetrics(pullRequests);
        this.generateAndPublishPullRequestsThroughputMetrics(pullRequests);

        LOGGER.debug("PullRequests scheduled task is finished.");
    }

    private void generateAndPublishPullRequestsThroughputMetrics(List<PullRequest> pullRequests) {
        THROUGHPUT_PERIODS.forEach(period -> {
            var throughput = this.getThroughputOfPullRequests(pullRequests, period);
            this.throughputGauges.get(period).set(throughput);
        });
    }

    private void generateAndPublishPullRequestCountsMetrics(List<PullRequest> pullRequests) {
        STATE_COUNT_PERIODS.forEach(period -> {
            var gaugeMap = this.stateCountGauges.get(period);
            var statesMap = this.getCountOfPullRequests(pullRequests, period);
            for (final var stateCount : statesMap.entrySet()) {
                gaugeMap.get(stateCount.getKey()).set(stateCount.getValue());
            }
        });
    }

    private Map<PullRequestState, Integer> getCountOfPullRequests(
            List<PullRequest> pullRequests,
            int period
    ) {
        var pullRequestExportMap = getEmptyPullRequestsMapByState();

        pullRequests.stream()
                .takeWhile(
                        pullRequest ->
                                pullRequest.getCreatedAt()
                                        .isAfter(ZonedDateTime.now().minusHours(period * 24L)))
                .forEach(pullRequest ->
                        pullRequestExportMap.put(
                                pullRequest.getState(),
                                1 + pullRequestExportMap.get(pullRequest.getState())));

        return pullRequestExportMap;

    }

    private Integer getThroughputOfPullRequests(List<PullRequest> pullRequests, int period) {
        AtomicInteger mergedCount = new AtomicInteger();
        pullRequests.stream()
                .takeWhile(
                        pullRequest ->
                                pullRequest.getCreatedAt()
                                        .isAfter(ZonedDateTime.now().minusHours(period * 24L)))
                .filter(pullRequest -> pullRequest.getState().equals(PullRequestState.MERGED))
                .forEach(pullRequest -> mergedCount.getAndIncrement());

        return mergedCount.get() / period;
    }

    private Map<PullRequestState, Integer> getEmptyPullRequestsMapByState() {
        Map<PullRequestState, Integer> pullRequestStateMap
                = new EnumMap<>(PullRequestState.class);

        Stream.of(PullRequestState.values()).forEach(
                pullRequestState -> pullRequestStateMap.put(pullRequestState, 0));

        return pullRequestStateMap;
    }

    private void initPullRequestGauges() {
        THROUGHPUT_PERIODS.forEach(period -> {
            var atomicInteger = new AtomicInteger();
            Gauge.builder(
                            getThroughputMetricName(period),
                            () -> atomicInteger
                    )
                    .strongReference(true)
                    .register(this.registry);
            this.throughputGauges.put(period, atomicInteger);
        });

        STATE_COUNT_PERIODS.forEach(period -> {
            this.stateCountGauges.put(period, new EnumMap<>(PullRequestState.class));
            Arrays.stream(PullRequestState.values()).forEach(state -> {
                var atomicInteger = new AtomicInteger();
                Gauge.builder(
                                getStateCountMetricName(period),
                                () -> atomicInteger
                        ).tag("state", state.toString())
                        .strongReference(true)
                        .register(this.registry);
                this.stateCountGauges.get(period).put(state, atomicInteger);
            });
        });
    }

    @Override
    public void run() {
        this.retrieveAndExportPullRequestsMetrics();
    }

    @Override
    public String cronExpression() {
        return this.cronExpression;
    }
}
