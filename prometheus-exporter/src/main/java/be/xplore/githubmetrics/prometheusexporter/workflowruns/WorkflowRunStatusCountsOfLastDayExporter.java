package be.xplore.githubmetrics.prometheusexporter.workflowruns;

import be.xplore.githubmetrics.domain.domain.WorkflowRun;
import be.xplore.githubmetrics.domain.usecases.GetAllWorkflowRunsOfLastDayUseCase;
import be.xplore.githubmetrics.prometheusexporter.ScheduledExporter;
import be.xplore.githubmetrics.prometheusexporter.SchedulingProperties;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

        Map<WorkflowRun.RunStatus, Integer> workflowRunsStatusCountsMap
                = this.getStatusCounts(workflowRuns);

        this.publishWorkflowRunsStatusCountsGauges(
                workflowRunsStatusCountsMap
        );

        LOGGER.info("Finished scheduled workflow runs task");
    }

    private void publishWorkflowRunsStatusCountsGauges(Map<WorkflowRun.RunStatus, Integer> statuses) {
        for (Map.Entry<WorkflowRun.RunStatus, Integer> entry : statuses.entrySet()) {
            Gauge.builder("workflow_runs",
                            entry,
                            Map.Entry::getValue
                    ).tag("status", entry.getKey().toString())
                    .strongReference(true)
                    .register(this.registry);
        }
    }

    private Map<WorkflowRun.RunStatus, Integer> createStatusCountsMap() {
        EnumMap<WorkflowRun.RunStatus, Integer> workflowRunStatusCountsMap = new EnumMap<>(WorkflowRun.RunStatus.class);

        Stream.of(WorkflowRun.RunStatus.values()).forEach(
                runStatus -> workflowRunStatusCountsMap.put(runStatus, 0));

        return workflowRunStatusCountsMap;
    }

    private Map<WorkflowRun.RunStatus, Integer> getStatusCounts(
            List<WorkflowRun> workflowRuns
    ) {
        Map<WorkflowRun.RunStatus, Integer> workflowRunStatusCountsMap
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
    public void run() {
        this.retrieveAndExportLastDaysWorkflowRunStatusCounts();
    }

    @Override
    public String cronExpression() {
        return this.cronExpression;
    }
}
