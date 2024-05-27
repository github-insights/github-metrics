package be.xplore.githubmetrics.domain.workflowrun;

import be.xplore.githubmetrics.domain.repository.RepositoriesQueryPort;
import be.xplore.githubmetrics.domain.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetAllWorkflowRunsOfLastDayUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetAllWorkflowRunsOfLastDayUseCase.class);

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
        LOGGER.info(
                "Exporting workflow runs of last day for {} repositories.",
                repositories.size()
        );
        return repositories.stream().map(
                workflowRunsQuery::getLastDaysWorkflowRuns
        ).flatMap(List::stream).toList();
    }
}
