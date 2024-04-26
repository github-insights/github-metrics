package be.xplore.githubmetrics.domain.schedulers;

import be.xplore.githubmetrics.domain.domain.WorkflowRun;
import be.xplore.githubmetrics.domain.exports.WorkflowRunsExportPort;
import be.xplore.githubmetrics.domain.providers.ports.RepositoriesProvider;
import be.xplore.githubmetrics.domain.providers.ports.WorkflowRunsProvider;
import be.xplore.githubmetrics.domain.schedulers.ports.WorkflowRunsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class WorkflowRunsRequestScheduler implements WorkflowRunsUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowRunsRequestScheduler.class);
    private final WorkflowRunsProvider workflowRunsProvider;
    private final List<WorkflowRunsExportPort> workflowRunsExportPorts;
    private final RepositoriesProvider getAllRepositoriesUseCase;

    public WorkflowRunsRequestScheduler(
            WorkflowRunsProvider workflowRunsProvider,
            List<WorkflowRunsExportPort> workflowRunsExportPorts,
            RepositoriesProvider getAllRepositoriesUseCase
    ) {
        this.workflowRunsProvider = workflowRunsProvider;
        this.workflowRunsExportPorts = workflowRunsExportPorts;
        this.getAllRepositoriesUseCase = getAllRepositoriesUseCase;
    }

    @Scheduled(fixedRate = 200, timeUnit = TimeUnit.SECONDS)
    @Override
    public void retrieveAndExportWorkflowRuns() {
        LOGGER.info("Running scheduled workflow runs task.");

        var allRepositories = this.getAllRepositoriesUseCase.getAllRepositories();

        var workflowRunsPerStatus = new EnumMap<WorkflowRun.RunStatus, Integer>(
                WorkflowRun.RunStatus.class
        );

        allRepositories.forEach(repository -> {
            var workflowRuns = this.workflowRunsProvider.getLastDaysWorkflowRuns(repository.getName());
            updateStatusCounts(workflowRuns, workflowRunsPerStatus);
        });

        this.workflowRunsExportPorts.forEach(port ->
                port.exportWorkflowRunsStatusCounts(workflowRunsPerStatus));

        LOGGER.info("Finished scheduled workflow runs task");
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
