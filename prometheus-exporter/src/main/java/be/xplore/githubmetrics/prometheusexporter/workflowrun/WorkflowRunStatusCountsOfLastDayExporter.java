package be.xplore.githubmetrics.prometheusexporter.workflowrun;

import be.xplore.githubmetrics.domain.workflowrun.GetAllWorkflowRunsOfLastDayUseCase;
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

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Service
public class WorkflowRunStatusCountsOfLastDayExporter implements ScheduledExporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowRunStatusCountsOfLastDayExporter.class);
    private final GetAllWorkflowRunsOfLastDayUseCase getAllWorkflowRunsOfLastDayUseCase;
    private final MeterRegistry registry;
    private final String cronExpression;
    private final Map<WorkflowRunStatus, AtomicInteger> statusCountsGauges = new EnumMap<>(WorkflowRunStatus.class);

    public WorkflowRunStatusCountsOfLastDayExporter(
            GetAllWorkflowRunsOfLastDayUseCase getAllWorkflowRunsOfLastDayUseCase,
            SchedulingProperties schedulingProperties,
            MeterRegistry registry
    ) {
        this.getAllWorkflowRunsOfLastDayUseCase = getAllWorkflowRunsOfLastDayUseCase;
        this.registry = registry;
        this.cronExpression = schedulingProperties.workflowRuns();
        this.initWorkflowStatusCountsGauges();
    }

    private void retrieveAndExportLastDaysWorkflowRunStatusCounts() {
        LOGGER.trace("LastDaysWorkflowRunStatusCounts scheduled task is running.");
        List<WorkflowRun> workflowRuns
                = getAllWorkflowRunsOfLastDayUseCase.getAllWorkflowRunsOfLastDay();

        Map<WorkflowRunStatus, Integer> workflowRunsStatusCountsMap
                = this.getStatusCounts(workflowRuns);

        workflowRunsStatusCountsMap.forEach((status, count) ->
                this.statusCountsGauges.get(status).set(count)
        );

        LOGGER.trace(
                "LastDaysWorkflowRunStatusCounts scheduled task has finished with {} different Status.",
                workflowRunsStatusCountsMap.size()
        );
    }

    private Map<WorkflowRunStatus, Integer> createStatusCountsMap() {
        Map<WorkflowRunStatus, Integer> workflowRunStatusCountsMap = new EnumMap<>(WorkflowRunStatus.class);

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

    private void initWorkflowStatusCountsGauges() {
        Arrays.stream(WorkflowRunStatus.values()).forEach(status -> {
            var atomicInteger = new AtomicInteger();
            Gauge.builder(
                            "workflow_runs",
                            () -> atomicInteger
                    ).tag("status", status.toString())
                    .strongReference(true)
                    .register(this.registry);
            this.statusCountsGauges.put(status, atomicInteger);
        });
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
