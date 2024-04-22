package be.xplore.githubmetrics.domain.scheduler;

import be.xplore.githubmetrics.domain.domain.WorkflowRun;
import be.xplore.githubmetrics.domain.usecases.WorkflowRunsUseCase;
import be.xplore.githubmetrics.domain.usecases.ports.in.WorkflowRunsQueryPort;
import be.xplore.githubmetrics.domain.usecases.ports.out.WorkflowRunsExportPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;

@Component
public class ApiRequestScheduler implements WorkflowRunsUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiRequestScheduler.class);
    private final WorkflowRunsQueryPort workflowRunsQueryPort;
    private final List<WorkflowRunsExportPort> workflowRunsExportPorts;

    public ApiRequestScheduler(WorkflowRunsQueryPort workflowRunsQueryPort, List<WorkflowRunsExportPort> workflowRunsExportPorts) {
        this.workflowRunsQueryPort = workflowRunsQueryPort;
        this.workflowRunsExportPorts = workflowRunsExportPorts;
    }

    @Scheduled(fixedRate = 5000)
    @Override
    public void retrieveAndExportWorkflowRuns() {
        LOGGER.info("Running scheduled workflow runs task.");
        var map = new EnumMap<WorkflowRun.RunStatus, Integer>(
                WorkflowRun.RunStatus.class
        );

        this.workflowRunsQueryPort.getLastDaysWorkflows().forEach(
                workflowRun -> {
                    var count = map.getOrDefault(
                            workflowRun.getStatus(),
                            0
                    );
                    count++;
                    map.put(workflowRun.getStatus(), count);
                });

        this.workflowRunsExportPorts.forEach(p ->
                p.exportWorkflowRunsStatusCounts(map)
        );
    }
}
