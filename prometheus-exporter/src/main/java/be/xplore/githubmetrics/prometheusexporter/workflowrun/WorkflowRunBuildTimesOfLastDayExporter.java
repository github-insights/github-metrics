package be.xplore.githubmetrics.prometheusexporter.workflowrun;

import be.xplore.githubmetrics.domain.workflowrun.GetAllWorkflowRunBuildTimesOfLastDayUseCase;
import be.xplore.githubmetrics.domain.workflowrun.WorkflowRun;
import be.xplore.githubmetrics.domain.workflowrun.WorkflowRunStatus;
import be.xplore.githubmetrics.prometheusexporter.ScheduledExporter;
import be.xplore.githubmetrics.prometheusexporter.config.SchedulingProperties;
import be.xplore.githubmetrics.prometheusexporter.features.FeatureAssociation;
import be.xplore.githubmetrics.prometheusexporter.features.Features;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.lang.Math.round;

@Service
public class WorkflowRunBuildTimesOfLastDayExporter implements ScheduledExporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowRunBuildTimesOfLastDayExporter.class);
    private static final String TOTAL_GAUGE_NAME = "workflow_runs_total_build_times";
    private static final String AVERAGE_GAUGE_NAME = "workflow_runs_average_build_times";
    private final GetAllWorkflowRunBuildTimesOfLastDayUseCase getAllWorkflowRunBuildTimesOfLastDayUseCase;
    private final MeterRegistry registry;
    private final String cronExpression;
    private final Map<String, Map<WorkflowRunStatus, AtomicInteger>> gaugesMap = new HashMap<>();

    public WorkflowRunBuildTimesOfLastDayExporter(
            GetAllWorkflowRunBuildTimesOfLastDayUseCase
                    getAllWorkflowRunBuildTimesOfLastDayUseCase,
            SchedulingProperties schedulingProperties,
            MeterRegistry registry
    ) {
        this.getAllWorkflowRunBuildTimesOfLastDayUseCase
                = getAllWorkflowRunBuildTimesOfLastDayUseCase;
        this.registry = registry;
        this.cronExpression = schedulingProperties.workflowRunBuildTimes();
        this.initWorkflowBuildTimesGauges();
    }

    private void retrieveAndExportLastDaysWorkflowRunBuildTimes() {
        LOGGER.trace("LastDaysWorkflowRunBuildTimes scheduled task is running.");
        List<WorkflowRun> workflowRuns =
                getAllWorkflowRunBuildTimesOfLastDayUseCase.getAllWorkflowRunBuildTime();

        this.getTotalBuildTimes(workflowRuns).forEach((status, total) ->
                this.gaugesMap.get(TOTAL_GAUGE_NAME).get(status).set(total)
        );
        this.getAverageBuildTimes(workflowRuns).forEach((status, average) ->
                this.gaugesMap.get(AVERAGE_GAUGE_NAME).get(status).set(average)
        );

        LOGGER.trace("LastDaysWorkflowRunBuildTimes scheduled task finished.");
    }

    private Map<WorkflowRunStatus, Integer> getAverageBuildTimes(
            List<WorkflowRun> workflowRuns
    ) {
        Map<WorkflowRunStatus, Integer> workflowRunsExportMap =
                this.createExportMap();
        for (var workflowRunsOfStatus : this.getWorkflowRunsByStatus(workflowRuns).entrySet()) {
            workflowRunsExportMap.put(
                    workflowRunsOfStatus.getKey(),
                    (int) round(workflowRunsOfStatus.getValue()
                            .stream()
                            .mapToDouble(WorkflowRun::getBuildTime)
                            .average()
                            .orElse(0.0)));
        }
        return workflowRunsExportMap;
    }

    private Map<WorkflowRunStatus, List<WorkflowRun>> getWorkflowRunsByStatus(
            List<WorkflowRun> workflowRuns
    ) {
        Map<WorkflowRunStatus, List<WorkflowRun>> workflowRunByStatusMap =
                this.getEmptyWorkflowRunsByStatus();

        workflowRuns.forEach(workflowRun ->
                workflowRunByStatusMap.get(workflowRun.getStatus()).add(workflowRun)
        );

        return workflowRunByStatusMap;
    }

    private Map<WorkflowRunStatus, List<WorkflowRun>> getEmptyWorkflowRunsByStatus() {
        Map<WorkflowRunStatus, List<WorkflowRun>> workflowRunsByStatusMap
                = new EnumMap<>(WorkflowRunStatus.class);

        Stream.of(WorkflowRunStatus.values()).forEach(
                runStatus -> workflowRunsByStatusMap.put(runStatus, new ArrayList<>()));

        return workflowRunsByStatusMap;
    }

    private Map<WorkflowRunStatus, Integer> getTotalBuildTimes(
            List<WorkflowRun> workflowRuns
    ) {
        Map<WorkflowRunStatus, Integer> workflowRunsExportMap =
                this.createExportMap();
        for (var workflowRunsOfStatus : this.getWorkflowRunsByStatus(workflowRuns).entrySet()) {
            workflowRunsExportMap.put(
                    workflowRunsOfStatus.getKey(),
                    (int) round(workflowRunsOfStatus.getValue()
                            .stream()
                            .mapToDouble(WorkflowRun::getBuildTime)
                            .sum()));
        }
        return workflowRunsExportMap;
    }

    private Map<WorkflowRunStatus, Integer> createExportMap() {
        Map<WorkflowRunStatus, Integer> workflowRunExportMap = new EnumMap<>(WorkflowRunStatus.class);

        Stream.of(WorkflowRunStatus.values()).forEach(
                runStatus -> workflowRunExportMap.put(runStatus, 0));

        return workflowRunExportMap;
    }

    private void initWorkflowBuildTimesGauges() {
        this.gaugesMap.put(TOTAL_GAUGE_NAME, new EnumMap<>(WorkflowRunStatus.class));
        this.gaugesMap.put(AVERAGE_GAUGE_NAME, new EnumMap<>(WorkflowRunStatus.class));
        Arrays.stream(WorkflowRunStatus.values()).forEach(status -> {
            var totalAtomicInteger = new AtomicInteger();
            var averageAtomicInteger = new AtomicInteger();
            Gauge.builder(
                            TOTAL_GAUGE_NAME,
                            () -> totalAtomicInteger
                    ).tag("status", status.toString())
                    .strongReference(true)
                    .register(this.registry);
            Gauge.builder(
                            AVERAGE_GAUGE_NAME,
                            () -> averageAtomicInteger
                    ).tag("status", status.toString())
                    .strongReference(true)
                    .register(this.registry);

            this.gaugesMap.get(TOTAL_GAUGE_NAME).put(
                    status, totalAtomicInteger
            );
            this.gaugesMap.get(AVERAGE_GAUGE_NAME).put(
                    status, averageAtomicInteger
            );
        });

    }

    @Override
    @FeatureAssociation(value = Features.EXPORTER_WORKFLOW_RUN_BUILD_TIMES_FEATURE)
    public void run() {
        this.retrieveAndExportLastDaysWorkflowRunBuildTimes();
    }

    @Override
    public String cronExpression() {
        return this.cronExpression;
    }
}
