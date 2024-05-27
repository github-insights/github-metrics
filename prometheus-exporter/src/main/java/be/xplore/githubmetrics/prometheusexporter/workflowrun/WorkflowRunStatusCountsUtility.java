package be.xplore.githubmetrics.prometheusexporter.workflowrun;

import be.xplore.githubmetrics.domain.workflowrun.WorkflowRun;
import be.xplore.githubmetrics.domain.workflowrun.WorkflowRunStatus;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

class WorkflowRunStatusCountsUtility {
    private WorkflowRunStatusCountsUtility() {
        throw new IllegalStateException("Utility class");
    }

    static Map<WorkflowRunStatus, Integer> createStatusCountsMap() {
        Map<WorkflowRunStatus, Integer> workflowRunStatusCountsMap = new EnumMap<>(WorkflowRunStatus.class);

        Stream.of(WorkflowRunStatus.values()).forEach(
                runStatus -> workflowRunStatusCountsMap.put(runStatus, 0));

        return workflowRunStatusCountsMap;
    }

    static Map<WorkflowRunStatus, Integer> getStatusCounts(
            List<WorkflowRun> workflowRuns
    ) {
        Map<WorkflowRunStatus, Integer> workflowRunStatusCountsMap
                = createStatusCountsMap();

        workflowRuns.forEach(workflowRun ->
                workflowRunStatusCountsMap.put(
                        workflowRun.getStatus(),
                        1 + workflowRunStatusCountsMap.get(workflowRun.getStatus())
                )
        );

        return workflowRunStatusCountsMap;
    }

    static Map<WorkflowRunStatus, AtomicInteger> initWorkflowStatusCountsGauges(MeterRegistry registry, String name) {
        Map<WorkflowRunStatus, AtomicInteger> statusCountsGauges
                = new EnumMap<>(WorkflowRunStatus.class);
        Arrays.stream(WorkflowRunStatus.values()).forEach(status -> {
            var atomicInteger = new AtomicInteger();
            Gauge.builder(
                            name,
                            () -> atomicInteger
                    ).tag("status", status.toString())
                    .strongReference(true)
                    .register(registry);
            statusCountsGauges.put(status, atomicInteger);
        });

        return statusCountsGauges;
    }

}
