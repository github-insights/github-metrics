package be.xplore.githubmetrics.prometheusexporter.workflowrun;

import be.xplore.githubmetrics.domain.workflowrun.GetAllWorkflowRunsOfLastDayUseCase;
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

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class WorkflowRunStatusCountsOfLastDayExporter implements ScheduledExporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowRunStatusCountsOfLastDayExporter.class);
    private final GetAllWorkflowRunsOfLastDayUseCase getAllWorkflowRunsOfLastDayUseCase;
    private final MeterRegistry registry;
    private final String cronExpression;

    public WorkflowRunStatusCountsOfLastDayExporter(
            GetAllWorkflowRunsOfLastDayUseCase getAllWorkflowRunsOfLastDayUseCase,
            SchedulingProperties schedulingProperties,
            MeterRegistry registry
    ) {
        this.getAllWorkflowRunsOfLastDayUseCase = getAllWorkflowRunsOfLastDayUseCase;
        this.registry = registry;
        this.cronExpression = schedulingProperties.workflowRunsInterval();
    }

    private void retrieveAndExportLastDaysWorkflowRunStatusCounts() {
        LOGGER.info("Running scheduled workflow runs task.");
        List<WorkflowRun> workflowRuns
                = getAllWorkflowRunsOfLastDayUseCase.getAllWorkflowRunsOfLastDay();

        Map<WorkflowRunStatus, Integer> workflowRunsStatusCountsMap
                = this.getStatusCounts(workflowRuns);

        this.publishWorkflowRunsStatusCountsGauges(
                workflowRunsStatusCountsMap
        );

        LOGGER.info("Finished scheduled workflow runs task");
    }

    private void publishWorkflowRunsStatusCountsGauges(Map<WorkflowRunStatus, Integer> statuses) {
        for (Map.Entry<WorkflowRunStatus, Integer> entry : statuses.entrySet()) {
            Gauge.builder("workflow_runs",
                            entry,
                            Map.Entry::getValue
                    ).tag("status", entry.getKey().toString())
                    .strongReference(true)
                    .register(this.registry);
        }
    }

    private Map<WorkflowRunStatus, Integer> createStatusCountsMap() {
        EnumMap<WorkflowRunStatus, Integer> workflowRunStatusCountsMap = new EnumMap<>(WorkflowRunStatus.class);

        Stream.of(WorkflowRunStatus.values()).forEach(
                runStatus -> workflowRunStatusCountsMap.put(runStatus, 0));

        return workflowRunStatusCountsMap;
    }

    private Map<WorkflowRunStatus, Integer> getStatusCounts(
            List<WorkflowRun> workflowRuns
    ) {
        Map<WorkflowRunStatus, Integer> workflowRunStatusCountsMap
                = this.createStatusCountsMap();

        workflowRuns.forEach(workflowRun ->
                workflowRunStatusCountsMap.put(
                        workflowRun.getStatus(),
                        1 + workflowRunStatusCountsMap.get(workflowRun.getStatus())
                )
        );

        return workflowRunStatusCountsMap;
    }

    @Override
    @CacheEvict(value = "WorkflowRuns", allEntries = true, beforeInvocation = true)
    public void run() {
        this.retrieveAndExportLastDaysWorkflowRunStatusCounts();
    }

    @Override
    public String cronExpression() {
        return this.cronExpression;
    }
}
