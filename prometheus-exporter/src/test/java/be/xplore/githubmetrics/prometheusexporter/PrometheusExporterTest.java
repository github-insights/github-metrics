package be.xplore.githubmetrics.prometheusexporter;

import be.xplore.githubmetrics.domain.domain.WorkflowRun;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PrometheusExporterTest {

    private final MeterRegistry registry = new SimpleMeterRegistry();

    @Test
    void testPrometheusExporter() {
        PrometheusExporter prometheusExporter = new PrometheusExporter(this.registry);
        Map<WorkflowRun.RunStatus, Integer> workflowRunStatuses = new HashMap<>();
        workflowRunStatuses.put(WorkflowRun.RunStatus.FAILED, 5);
        prometheusExporter.exportWorkflowRunsStatusCounts(workflowRunStatuses);
        assertEquals(
                5,
                registry.get("workflow_runs").gauge().value()
        );
    }

}
