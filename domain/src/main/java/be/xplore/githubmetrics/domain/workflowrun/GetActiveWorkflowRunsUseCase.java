package be.xplore.githubmetrics.domain.workflowrun;

import be.xplore.githubmetrics.domain.repository.RepositoriesQueryPort;
import be.xplore.githubmetrics.domain.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetActiveWorkflowRunsUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetActiveWorkflowRunsUseCase.class);
    private final RepositoriesQueryPort repositoriesQuery;
    private final ActiveWorkflowRunsQueryPort activeWorkflowRunsQuery;

    public GetActiveWorkflowRunsUseCase(
            RepositoriesQueryPort repositoriesQuery,
            ActiveWorkflowRunsQueryPort activeWorkflowRunsQuery
    ) {
        this.repositoriesQuery = repositoriesQuery;
        this.activeWorkflowRunsQuery = activeWorkflowRunsQuery;
    }

    public List<WorkflowRun> getActiveWorkflowRuns() {
        List<Repository> repositories = repositoriesQuery.getAllRepositories();
        LOGGER.debug(
                "Exporting active workflow runs for {} repositories.",
                repositories.size()
        );
        return repositories.stream().map(
                activeWorkflowRunsQuery::getActiveWorkflowRuns
        ).flatMap(List::stream).toList();
    }
}
