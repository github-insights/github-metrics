package be.xplore.githubmetrics.domain.scheduler;

import be.xplore.githubmetrics.domain.domain.WorkflowRun;
import be.xplore.githubmetrics.domain.usecases.GetAllRepositoriesUseCase;
import be.xplore.githubmetrics.domain.usecases.WorkflowRunsUseCase;
import be.xplore.githubmetrics.domain.usecases.ports.in.WorkflowRunsQueryPort;
import be.xplore.githubmetrics.domain.usecases.ports.out.WorkflowRunsExportPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ApiRequestScheduler implements WorkflowRunsUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiRequestScheduler.class);
    private final GetAllRepositoriesUseCase getAllRepositoriesUseCase;
    private final WorkflowRunsQueryPort workflowRunsQueryPort;
    private final List<WorkflowRunsExportPort> workflowRunsExportPorts;

    public ApiRequestScheduler(
            GetAllRepositoriesUseCase getAllRepositoriesUseCase,
            WorkflowRunsQueryPort workflowRunsQueryPort,
            List<WorkflowRunsExportPort> workflowRunsExportPorts
    ) {
        this.getAllRepositoriesUseCase = getAllRepositoriesUseCase;
        this.workflowRunsQueryPort = workflowRunsQueryPort;
        this.workflowRunsExportPorts = workflowRunsExportPorts;
    }

    @Scheduled(fixedRate = 5000)
    @Override
    public void retrieveAndExportWorkflowRuns() {
        LOGGER.info("Running scheduled workflow runs task.");

        var allRepositories = this.getAllRepositoriesUseCase.getAllRepositories();

        var workflowRunsPerStatus = new EnumMap<WorkflowRun.RunStatus, Integer>(
                WorkflowRun.RunStatus.class
        );

        allRepositories.forEach(repository -> {
            var workflowRuns = this.workflowRunsQueryPort.getLastDaysWorkflows(repository.getName());
            updateStatusCounts(workflowRuns, workflowRunsPerStatus);
        });

        this.workflowRunsExportPorts.forEach(port ->
                port.exportWorkflowRunsStatusCounts(workflowRunsPerStatus)
        );
    }

    private void updateStatusCounts(
            List<WorkflowRun> workflowRuns,
            Map<WorkflowRun.RunStatus, Integer> workflowRunsPerStatus
    ) {
        workflowRuns.forEach(workflowRun ->
                workflowRunsPerStatus.put(
                        workflowRun.getStatus(),
                        1 + workflowRunsPerStatus.getOrDefault(
                                workflowRun.getStatus(),
                                0
                        )
                )
        );
    }
}
