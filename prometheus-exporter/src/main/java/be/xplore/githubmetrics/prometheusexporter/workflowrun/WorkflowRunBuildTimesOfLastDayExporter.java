package be.xplore.githubmetrics.prometheusexporter.workflowrun;

import be.xplore.githubmetrics.domain.workflowrun.GetAllWorkflowRunBuildTimesOfLastDayUseCase;
import be.xplore.githubmetrics.domain.workflowrun.WorkflowRun;
import be.xplore.githubmetrics.domain.workflowrun.WorkflowRunStatus;
import be.xplore.githubmetrics.prometheusexporter.ScheduledExporter;
import be.xplore.githubmetrics.prometheusexporter.config.SchedulingProperties;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.Math.round;

@Service
public class WorkflowRunBuildTimesOfLastDayExporter implements ScheduledExporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowRunBuildTimesOfLastDayExporter.class);
    private final GetAllWorkflowRunBuildTimesOfLastDayUseCase getAllWorkflowRunBuildTimesOfLastDayUseCase;
    private final MeterRegistry registry;
    private final String cronExpression;

    public WorkflowRunBuildTimesOfLastDayExporter(
            GetAllWorkflowRunBuildTimesOfLastDayUseCase
                    getAllWorkflowRunBuildTimesOfLastDayUseCase,
            SchedulingProperties schedulingProperties,
            MeterRegistry registry
    ) {
        this.getAllWorkflowRunBuildTimesOfLastDayUseCase
                = getAllWorkflowRunBuildTimesOfLastDayUseCase;
        this.registry = registry;
        this.cronExpression = schedulingProperties.workflowRunBuildTimesInterval();
    }

    private void retrieveAndExportLastDaysWorkflowRunBuildTimes() {
        LOGGER.info("LastDaysWorkflowRunBuildTimes scheduled task is running.");
        List<WorkflowRun> workflowRuns =
                getAllWorkflowRunBuildTimesOfLastDayUseCase.getAllWorkflowRunBuildTime();

        var buildTimesMap = new HashMap<String, Map<WorkflowRunStatus, Integer>>();

        buildTimesMap.put("workflow_runs_total_build_times", this.getTotalBuildTimes(workflowRuns));
        buildTimesMap.put("workflow_runs_average_build_times", this.getAverageBuildTimes(workflowRuns));

        this.publishWorkflowRunsBuildTimesGauges(buildTimesMap);

        LOGGER.debug("LastDaysWorkflowRunBuildTimes scheduled task finished.");
    }

    private void publishWorkflowRunsBuildTimesGauges(
            Map<String, Map<WorkflowRunStatus, Integer>> buildTimesMap
    ) {
        for (Map.Entry<String, Map<WorkflowRunStatus, Integer>> metric : buildTimesMap.entrySet()) {
            for (Map.Entry<WorkflowRunStatus, Integer> entry : metric.getValue().entrySet()) {
                Gauge.builder(metric.getKey(),
                                entry,
                                Map.Entry::getValue
                        ).tag("status", entry.getKey().toString())
                        .strongReference(true)
                        .register(this.registry);

            }

        }
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

    @Override
    @CacheEvict(value = "WorkflowRunBuildTimes", allEntries = true, beforeInvocation = true)
    public void run() {
        this.retrieveAndExportLastDaysWorkflowRunBuildTimes();
    }

    @Override
    public String cronExpression() {
        return this.cronExpression;
    }
}
