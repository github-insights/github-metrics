package be.xplore.githubmetrics.domain.pullrequest;

import be.xplore.githubmetrics.domain.repository.RepositoriesQueryPort;
import be.xplore.githubmetrics.domain.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetAllPullRequestsUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetAllPullRequestsUseCase.class);

    private final RepositoriesQueryPort repositoriesQuery;
    private final PullRequestQueryPort pullRequestQuery;

    public GetAllPullRequestsUseCase(
            RepositoriesQueryPort repositoriesQuery,
            PullRequestQueryPort pullRequestQuery
    ) {
        this.repositoriesQuery = repositoriesQuery;
        this.pullRequestQuery = pullRequestQuery;
    }

    public List<PullRequest> getAllPullRequests() {
        List<Repository> repositories = repositoriesQuery.getAllRepositories();
        LOGGER.info("Exporting pull requests for {} repositories.", repositories.size());
        return repositories.stream().map(
                pullRequestQuery::getAllPullRequestsForRepository
        ).flatMap(List::stream).toList();
    }
}
