package be.xplore.githubmetrics.domain.pullrequest;

import be.xplore.githubmetrics.domain.repository.Repository;

import java.util.List;

public interface PullRequestQueryPort {
    List<PullRequest> getAllPullRequestsForRepository(Repository repository);
}
