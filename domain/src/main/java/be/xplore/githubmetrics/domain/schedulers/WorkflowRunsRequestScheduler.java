package be.xplore.githubmetrics.domain.schedulers;

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
import java.util.concurrent.TimeUnit;

@Component
public class WorkflowRunsRequestScheduler implements WorkflowRunsUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowRunsRequestScheduler.class);
    private final WorkflowRunsQueryPort workflowRunsQueryPort;
    private final List<WorkflowRunsExportPort> workflowRunsExportPorts;
    private final GetAllRepositoriesUseCase getAllRepositoriesUseCase;

    public WorkflowRunsRequestScheduler(
            WorkflowRunsQueryPort workflowRunsQueryPort,
            List<WorkflowRunsExportPort> workflowRunsExportPorts,
            GetAllRepositoriesUseCase getAllRepositoriesUseCase
    ) {
        this.workflowRunsQueryPort = workflowRunsQueryPort;
        this.workflowRunsExportPorts = workflowRunsExportPorts;
        this.getAllRepositoriesUseCase = getAllRepositoriesUseCase;
    }

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.SECONDS)
    @Override
    public void retrieveAndExportWorkflowRuns() {
        LOGGER.info("Running scheduled workflow runs task.");

        var allRepositories = this.getAllRepositoriesUseCase.getAllRepositories();

        var countsMap = new EnumMap<WorkflowRun.RunStatus, Integer>(
                WorkflowRun.RunStatus.class
        );

        allRepositories.forEach(repository ->
                this.addActionsToCounts(repository.getName(), countsMap));

        this.workflowRunsExportPorts.forEach(p ->
                p.exportWorkflowRunsStatusCounts(countsMap));

        LOGGER.info("Finished scheduled workflow runs task");
    }

    private void addActionsToCounts(
            String repositoryName,
            EnumMap<WorkflowRun.RunStatus, Integer> counts
    ) {
        this.workflowRunsQueryPort.getLastDaysWorkflows(repositoryName).forEach(
                workflowRun -> {
                    var count = counts.getOrDefault(
                            workflowRun.getStatus(),
                            0
                    );
                    count++;
                    counts.put(workflowRun.getStatus(), count);
                });
    }

}
