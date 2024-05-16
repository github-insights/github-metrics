package be.xplore.githubmetrics.prometheusexporter.pullrequest;

import be.xplore.githubmetrics.domain.pullrequest.GetAllPullRequestsUseCase;
import be.xplore.githubmetrics.domain.pullrequest.PullRequest;
import be.xplore.githubmetrics.domain.pullrequest.PullRequestState;
import be.xplore.githubmetrics.prometheusexporter.ScheduledExporter;
import be.xplore.githubmetrics.prometheusexporter.config.SchedulingProperties;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class PullRequestExporter implements ScheduledExporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(PullRequestExporter.class);

    private final GetAllPullRequestsUseCase getAllPullRequestsUseCase;
    private final MeterRegistry registry;

    private final String cronExpression;

    public PullRequestExporter(
            GetAllPullRequestsUseCase getAllPullRequestsUseCase,
            SchedulingProperties schedulingProperties,
            MeterRegistry registry
    ) {
        this.getAllPullRequestsUseCase = getAllPullRequestsUseCase;
        this.registry = registry;
        this.cronExpression = schedulingProperties.pullRequestsInterval();
    }

    private void retrieveAndExportPullRequestsMetrics() {
        LOGGER.info("PullRequests scheduled task is running.");
        List<PullRequest> pullRequests = getAllPullRequestsUseCase.getAllPullRequests();

        var pullRequestMapByPeriod = new HashMap<String, Map<PullRequestState, Integer>>();

        pullRequestMapByPeriod.put(
                "pull_requests_count_of_last_1_days",
                this.getCountOfPullRequests(pullRequests, 1)
        );
        pullRequestMapByPeriod.put(
                "pull_requests_count_of_last_2_days",
                this.getCountOfPullRequests(pullRequests, 2)
        );
        pullRequestMapByPeriod.put(
                "pull_requests_count_of_last_7_days",
                this.getCountOfPullRequests(pullRequests, 7)
        );

        this.publishPullRequestMetrics(pullRequestMapByPeriod);

        LOGGER.debug("PullRequests scheduled task is finished.");
    }

    private void publishPullRequestMetrics(
            Map<String, Map<PullRequestState, Integer>> pullRequestsMetricsMap
    ) {
        for (var pullRequestMetric : pullRequestsMetricsMap.entrySet()) {
            for (var entry : pullRequestMetric.getValue().entrySet()) {
                Gauge.builder(pullRequestMetric.getKey(),
                                entry,
                                Map.Entry::getValue
                        ).tag("state", entry.getKey().toString())
                        .strongReference(true)
                        .register(this.registry);
            }
        }
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

    private Map<PullRequestState, Integer> getEmptyPullRequestsMapByState() {
        Map<PullRequestState, Integer> pullRequestStateMap
                = new EnumMap<>(PullRequestState.class);

        Stream.of(PullRequestState.values()).forEach(
                pullRequestState -> pullRequestStateMap.put(pullRequestState, 0));

        return pullRequestStateMap;
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
