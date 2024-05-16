package be.xplore.githubmetrics.domain.pullrequest;

import be.xplore.githubmetrics.domain.repository.RepositoriesQueryPort;
import be.xplore.githubmetrics.domain.repository.Repository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetAllPullRequestsUseCase {
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
        return repositories.stream().map(
                pullRequestQuery::getAllPullRequestsForRepository
        ).flatMap(List::stream).toList();
    }
}
