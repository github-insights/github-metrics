package be.xplore.githubmetrics.prometheusexporter.selfhostedrunner;

import be.xplore.githubmetrics.domain.selfhostedrunner.GetAllSelfHostedRunnersUseCase;
import be.xplore.githubmetrics.domain.selfhostedrunner.OperatingSystem;
import be.xplore.githubmetrics.domain.selfhostedrunner.SelfHostedRunner;
import be.xplore.githubmetrics.domain.selfhostedrunner.SelfHostedRunnerStatus;
import be.xplore.githubmetrics.prometheusexporter.ScheduledExporter;
import be.xplore.githubmetrics.prometheusexporter.config.SchedulingProperties;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

@Service
public class SelfHostedRunnerCountsExporter implements ScheduledExporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelfHostedRunnerCountsExporter.class);
    private static final String GAUGE_NAME = "self_hosted_runners";
    private final GetAllSelfHostedRunnersUseCase getAllSelfHostedRunnersUseCase;
    private final MeterRegistry registry;
    private final String cronExpression;
    private final Map<SelfHostedRunnerState, AtomicInteger> gauges = new HashMap<>();

    public SelfHostedRunnerCountsExporter(
            GetAllSelfHostedRunnersUseCase getAllSelfHostedRunnersUseCase,
            SchedulingProperties schedulingProperties,
            MeterRegistry registry

    ) {
        this.getAllSelfHostedRunnersUseCase = getAllSelfHostedRunnersUseCase;
        this.registry = registry;
        this.cronExpression = schedulingProperties.selfHostedRunnersInterval();

        allStateCombinations((os, status) -> {
            var integer = new AtomicInteger(0);
            Gauge.builder(
                            GAUGE_NAME,
                            () -> integer
                    )
                    .tag("os", os.toString())
                    .tag("status", status.toString())
                    .strongReference(true)
                    .register(this.registry);
            gauges.put(
                    new SelfHostedRunnerState(os, status),
                    integer
            );
        });

    }

    private void retrieveAndExportSelfHostedRunnerCounts() {
        LOGGER.info("SelfHostedRunnerCounts scheduled task is running.");
        List<SelfHostedRunner> selfHostedRunners = this.getAllSelfHostedRunnersUseCase.getAllSelfHostedRunners();
        var runnerStates = this.countSelfHostedRunnerStates(selfHostedRunners);

        for (Map.Entry<SelfHostedRunnerState, Integer> entry : runnerStates.entrySet()) {
            this.gauges.get(entry.getKey()).set(entry.getValue());
        }

        LOGGER.debug("SelfHostedRunnerCounts scheduled task finished.");
    }

    private Map<SelfHostedRunnerState, Integer> countSelfHostedRunnerStates(
            List<SelfHostedRunner> runners
    ) {
        var map = this.getStatesMap();
        runners.forEach(runner -> {
            var runnerState = new SelfHostedRunnerState(
                    runner.getOperatingSystem(),
                    runner.getSelfHostedRunnerStatus()
            );
            map.put(runnerState, map.getOrDefault(runnerState, 0) + 1);
        });
        return map;
    }

    private Map<SelfHostedRunnerState, Integer> getStatesMap() {
        Map<SelfHostedRunnerState, Integer> map = new HashMap<>();
        allStateCombinations((os, state) -> map.put(new SelfHostedRunnerState(os, state), 0));
        return map;
    }

    @Override
    public void run() {
        this.retrieveAndExportSelfHostedRunnerCounts();
    }

    @Override
    public String cronExpression() {
        return this.cronExpression;
    }

    private void allStateCombinations(BiConsumer<OperatingSystem, SelfHostedRunnerStatus> function) {
        Arrays.stream(OperatingSystem.values()).forEach(os ->
                Arrays.stream(SelfHostedRunnerStatus.values()).forEach(
                        state -> function.accept(os, state)
                )
        );
    }
}
