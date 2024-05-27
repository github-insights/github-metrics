package be.xplore.githubmetrics.prometheusexporter.workflowrun;

import be.xplore.githubmetrics.domain.workflowrun.GetAllWorkflowRunsOfLastDayUseCase;
import be.xplore.githubmetrics.domain.workflowrun.WorkflowRun;
import be.xplore.githubmetrics.domain.workflowrun.WorkflowRunStatus;
import be.xplore.githubmetrics.prometheusexporter.ScheduledExporter;
import be.xplore.githubmetrics.prometheusexporter.config.SchedulingProperties;
import be.xplore.githubmetrics.prometheusexporter.features.FeatureAssociation;
import be.xplore.githubmetrics.prometheusexporter.features.Features;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static be.xplore.githubmetrics.prometheusexporter.workflowrun.WorkflowRunStatusCountsUtility.getStatusCounts;
import static be.xplore.githubmetrics.prometheusexporter.workflowrun.WorkflowRunStatusCountsUtility.initWorkflowStatusCountsGauges;

@Service
public class WorkflowRunStatusCountsOfLastDayExporter implements ScheduledExporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowRunStatusCountsOfLastDayExporter.class);
    private final GetAllWorkflowRunsOfLastDayUseCase getAllWorkflowRunsOfLastDayUseCase;
    private final String cronExpression;
    private final Map<WorkflowRunStatus, AtomicInteger> statusCountsGauges;

    public WorkflowRunStatusCountsOfLastDayExporter(
            GetAllWorkflowRunsOfLastDayUseCase getAllWorkflowRunsOfLastDayUseCase,
            SchedulingProperties schedulingProperties,
            MeterRegistry registry
    ) {
        this.getAllWorkflowRunsOfLastDayUseCase = getAllWorkflowRunsOfLastDayUseCase;
        this.cronExpression = schedulingProperties.workflowRuns();
        this.statusCountsGauges = initWorkflowStatusCountsGauges(registry, "workflow_runs");
    }

    private void retrieveAndExportLastDaysWorkflowRunStatusCounts() {
        LOGGER.trace("LastDaysWorkflowRunStatusCounts scheduled task is running.");
        List<WorkflowRun> workflowRuns
                = getAllWorkflowRunsOfLastDayUseCase.getAllWorkflowRunsOfLastDay();

        Map<WorkflowRunStatus, Integer> workflowRunsStatusCountsMap
                = getStatusCounts(workflowRuns);

        workflowRunsStatusCountsMap.forEach((status, count) ->
                this.statusCountsGauges.get(status).set(count)
        );

        LOGGER.trace(
                "LastDaysWorkflowRunStatusCounts scheduled task has finished with {} different Status.",
                workflowRunsStatusCountsMap.size()
        );
    }

    @Override
    @FeatureAssociation(value = Features.EXPORTER_WORKFLOW_RUNS_FEATURE)
    public void run() {
        this.retrieveAndExportLastDaysWorkflowRunStatusCounts();
    }

    @Override
    public String cronExpression() {
        return this.cronExpression;
    }
}
