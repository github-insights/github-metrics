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

@Service
public class SelfHostedRunnerCountsExporter implements ScheduledExporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelfHostedRunnerCountsExporter.class);
    private final GetAllSelfHostedRunnersUseCase getAllSelfHostedRunnersUseCase;
    private final MeterRegistry registry;
    private final String cronExpression;

    public SelfHostedRunnerCountsExporter(
            GetAllSelfHostedRunnersUseCase getAllSelfHostedRunnersUseCase,
            SchedulingProperties schedulingProperties,
            MeterRegistry registry

    ) {
        this.getAllSelfHostedRunnersUseCase = getAllSelfHostedRunnersUseCase;
        this.registry = registry;
        this.cronExpression = schedulingProperties.selfHostedRunnersInterval();
    }

    private void retrieveAndExportSelfHostedRunnerCounts() {
        LOGGER.info("SelfHostedRunnerCounts scheduled task is running.");
        List<SelfHostedRunner> selfHostedRunners = this.getAllSelfHostedRunnersUseCase.getAllSelfHostedRunners();
        var runnerStates = this.countSelfHostedRunnerStates(selfHostedRunners);

        for (Map.Entry<SelfHostedRunnerState, Integer> entry : runnerStates.entrySet()) {
            Gauge.builder(
                            "self_hosted_runners",
                            entry,
                            Map.Entry::getValue
                    )
                    .tag("os", entry.getKey().operatingSystem().toString())
                    .tag("status", entry.getKey().status().toString())
                    .strongReference(true)
                    .register(this.registry);

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
        Arrays.stream(OperatingSystem.values()).forEach(os ->
                Arrays.stream(SelfHostedRunnerStatus.values()).forEach(
                        state -> map.put(new SelfHostedRunnerState(os, state), 0)
                )
        );
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
}
