package be.xplore.githubmetrics.domain.usecases;

import be.xplore.githubmetrics.domain.domain.Repository;
import be.xplore.githubmetrics.domain.domain.WorkflowRun;
import be.xplore.githubmetrics.domain.queries.RepositoriesQueryPort;
import be.xplore.githubmetrics.domain.queries.WorkflowRunsQueryPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultGetAllWorkflowRunsOfLastDayUseCase implements GetAllWorkflowRunsOfLastDayUseCase {
    private final RepositoriesQueryPort repositoriesQuery;

    private final WorkflowRunsQueryPort workflowRunsQuery;

    public DefaultGetAllWorkflowRunsOfLastDayUseCase(
            RepositoriesQueryPort repositoriesQuery,
            WorkflowRunsQueryPort workflowRunsQuery) {
        this.repositoriesQuery = repositoriesQuery;
        this.workflowRunsQuery = workflowRunsQuery;
    }

    @Override
    public List<WorkflowRun> getAllWorkflowRunsOfLastDay() {
        List<Repository> repositories = repositoriesQuery.getAllRepositories();
        return repositories.stream().map(
                workflowRunsQuery::getLastDaysWorkflowRuns
        ).flatMap(List::stream).toList();
    }
}
