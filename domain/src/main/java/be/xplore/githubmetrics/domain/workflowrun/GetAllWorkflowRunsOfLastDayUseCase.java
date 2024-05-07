package be.xplore.githubmetrics.domain.workflowrun;

import be.xplore.githubmetrics.domain.repository.RepositoriesQueryPort;
import be.xplore.githubmetrics.domain.repository.Repository;
import be.xplore.githubmetrics.domain.workflowrun.model.WorkflowRun;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetAllWorkflowRunsOfLastDayUseCase {
    private final RepositoriesQueryPort repositoriesQuery;

    private final WorkflowRunsQueryPort workflowRunsQuery;

    public GetAllWorkflowRunsOfLastDayUseCase(
            RepositoriesQueryPort repositoriesQuery,
            WorkflowRunsQueryPort workflowRunsQuery) {
        this.repositoriesQuery = repositoriesQuery;
        this.workflowRunsQuery = workflowRunsQuery;
    }

    public List<WorkflowRun> getAllWorkflowRunsOfLastDay() {
        List<Repository> repositories = repositoriesQuery.getAllRepositories();
        return repositories.stream().map(
                workflowRunsQuery::getLastDaysWorkflowRuns
        ).flatMap(List::stream).toList();
    }
}
