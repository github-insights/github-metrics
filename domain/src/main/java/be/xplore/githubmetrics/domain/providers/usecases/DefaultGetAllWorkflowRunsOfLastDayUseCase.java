package be.xplore.githubmetrics.domain.providers.usecases;

import be.xplore.githubmetrics.domain.domain.Repository;
import be.xplore.githubmetrics.domain.domain.WorkflowRun;
import be.xplore.githubmetrics.domain.providers.ports.RepositoriesProvider;
import be.xplore.githubmetrics.domain.providers.ports.WorkflowRunsProvider;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultGetAllWorkflowRunsOfLastDayUseCase implements GetAllWorkflowRunsOfLastDayUseCase {
    private final RepositoriesProvider repositoriesProvider;
    private final WorkflowRunsProvider workflowRunsProvider;

    public DefaultGetAllWorkflowRunsOfLastDayUseCase(RepositoriesProvider repositoriesProvider, WorkflowRunsProvider workflowRunsProvider) {
        this.repositoriesProvider = repositoriesProvider;
        this.workflowRunsProvider = workflowRunsProvider;
    }

    @Override
    public List<WorkflowRun> getAllWorkflowRunsOfLastDay() {
        List<Repository> repositories = repositoriesProvider.getAllRepositories();
        return repositories.stream().map(
                workflowRunsProvider::getLastDaysWorkflowRuns
        ).flatMap(List::stream).toList();
    }
}
