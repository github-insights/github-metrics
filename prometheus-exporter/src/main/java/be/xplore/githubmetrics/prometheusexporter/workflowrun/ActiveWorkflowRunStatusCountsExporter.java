package be.xplore.githubmetrics.prometheusexporter.workflowrun;

import be.xplore.githubmetrics.domain.workflowrun.GetActiveWorkflowRunsUseCase;
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
public class ActiveWorkflowRunStatusCountsExporter implements ScheduledExporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ActiveWorkflowRunStatusCountsExporter.class);
    private final GetActiveWorkflowRunsUseCase getActiveWorkflowRunsUseCase;
    private final String cronExpression;
    private final Map<WorkflowRunStatus, AtomicInteger> statusCountsGauges;

    public ActiveWorkflowRunStatusCountsExporter(
            GetActiveWorkflowRunsUseCase getActiveWorkflowRunsUseCase,
            SchedulingProperties schedulingProperties,
            MeterRegistry registry
    ) {
        this.getActiveWorkflowRunsUseCase = getActiveWorkflowRunsUseCase;
        this.cronExpression = schedulingProperties.activeWorkflowRuns();
        this.statusCountsGauges = initWorkflowStatusCountsGauges(registry, "active_workflow_runs");
    }

    private void retrieveAndExportActiveWorkflowRunStatusCounts() {
        LOGGER.info("ActiveWorkflowRunStatusCounts scheduled task is running");
        List<WorkflowRun> activeWorkflowRuns
                = getActiveWorkflowRunsUseCase.getActiveWorkflowRuns();

        Map<WorkflowRunStatus, Integer> activeWorkflowRunsStatusCountsMap
                = getStatusCounts(activeWorkflowRuns);

        activeWorkflowRunsStatusCountsMap.forEach((status, count) ->
                this.statusCountsGauges.get(status).set(count)
        );
    }

    @Override
    @FeatureAssociation(value = Features.EXPORTER_ACTIVE_WORKFLOW_RUNS_FEATURE)
    public void run() {
        this.retrieveAndExportActiveWorkflowRunStatusCounts();
    }

    @Override
    public String cronExpression() {
        return this.cronExpression;
    }
}
