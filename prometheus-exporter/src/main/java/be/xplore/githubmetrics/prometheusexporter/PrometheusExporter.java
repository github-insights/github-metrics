package be.xplore.githubmetrics.prometheusexporter;

import be.xplore.githubmetrics.domain.domain.WorkflowRun;
import be.xplore.githubmetrics.domain.exports.WorkflowRunJobsExportPort;
import be.xplore.githubmetrics.domain.exports.WorkflowRunsExportPort;
import be.xplore.githubmetrics.domain.schedulers.WorkFlowRunJobsScheduler;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PrometheusExporter implements WorkflowRunsExportPort, WorkflowRunJobsExportPort {

    private final MeterRegistry registry;

    public PrometheusExporter(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void exportWorkflowRunsStatusCounts(
            Map<WorkflowRun.RunStatus, Integer> statuses
    ) {
        for (Map.Entry<WorkflowRun.RunStatus, Integer> entry : statuses.entrySet()) {
            Gauge.builder(
                    "workflow_runs." + entry.getKey().toString().toLowerCase(),
                    entry,
                    Map.Entry::getValue
            ).register(this.registry);
        }

    }

    @Override
    public void exportWorkflowRunJobsLabelsCounts(
            Map<WorkFlowRunJobsScheduler.JobLabels, Integer> statuses
    ) {
        for (Map.Entry<WorkFlowRunJobsScheduler.JobLabels, Integer> entry : statuses.entrySet()) {
            Gauge.builder("workflow_run_jobs",
                            entry,
                            Map.Entry::getValue)
                    .tag("conclusion", entry.getKey().conclusion().toString())
                    .tag("status", entry.getKey().status().toString())
                    .strongReference(true)
                    .register(this.registry);
        }

    }
}
