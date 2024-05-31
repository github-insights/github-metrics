package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.apistate.ApiRateLimitState;
import be.xplore.githubmetrics.domain.pullrequest.PullRequest;
import be.xplore.githubmetrics.domain.pullrequest.PullRequestQueryPort;
import be.xplore.githubmetrics.domain.repository.Repository;
import be.xplore.githubmetrics.githubadapter.cacheevicting.CacheEvictionProperties;
import be.xplore.githubmetrics.githubadapter.cacheevicting.ScheduledCacheEvictionPort;
import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import be.xplore.githubmetrics.githubadapter.config.GithubRestClient;
import be.xplore.githubmetrics.githubadapter.mappingclasses.GHPullRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PullRequestsAdapter implements PullRequestQueryPort, ScheduledCacheEvictionPort {
    private static final String PULL_REQUESTS_CACHE_NAME = "PullRequests";
    private static final Logger LOGGER = LoggerFactory.getLogger(PullRequestsAdapter.class);

    private final GithubProperties githubProperties;
    private final GithubRestClient restClient;
    private final GithubApiUtilities utilities;
    private final CacheEvictionProperties evictionProperties;
    private final ApiRateLimitState rateLimitState;

    public PullRequestsAdapter(
            GithubProperties githubProperties,
            GithubRestClient githubRestClient,
            GithubApiUtilities utilities,
            CacheEvictionProperties evictionProperties,
            ApiRateLimitState rateLimitState
    ) {
        this.githubProperties = githubProperties;
        this.restClient = githubRestClient;
        this.utilities = utilities;
        this.evictionProperties = evictionProperties;
        this.rateLimitState = rateLimitState;
    }

    @Cacheable(PULL_REQUESTS_CACHE_NAME)
    @Override
    public List<PullRequest> getAllPullRequestsForRepository(Repository repository) {
        LOGGER.debug(
                "Fetching fresh PullRequests for Repository {} {}",
                repository.getId(), repository.getName()
        );
        var parameters = new HashMap<String, String>();
        parameters.put("state", "all");
        parameters.put("per_page", "100");
        parameters.put("page", "0");

        var pullRequests = this.makeRequestAndFollowPagination(parameters, repository);

        LOGGER.debug(
                "Response for the PullRequests fetch of Repository {} returned {} PullRequests.",
                repository.getId(),
                pullRequests.size()
        );

        return pullRequests;
    }

    private List<PullRequest> makeRequestAndFollowPagination(Map<String, String> parameters, Repository repository) {
        ResponseEntity<GHPullRequest[]> responseEntity
                = this.restClient.getPullRequests(
                githubProperties.org(),
                repository.getName(),
                parameters);

        return this.utilities.followPaginationLink(
                responseEntity,
                1,
                pageNumber -> {
                    parameters.put("page", String.valueOf(pageNumber));
                    return this.restClient.getPullRequests(
                            githubProperties.org(),
                            repository.getName(),
                            parameters);
                },
                ghPullRequests -> Arrays.stream(ghPullRequests)
                        .map(GHPullRequest::getPullRequest)
                        .toList()
        );
    }

    @Override
    public boolean freshDataCanWait() {
        return this.rateLimitState.shouldDataWait(
                this.evictionProperties.pullRequests().status()
        );
    }

    @Override
    public String cacheName() {
        return PULL_REQUESTS_CACHE_NAME;
    }

    @Override
    public String cronExpression() {
        return this.evictionProperties.pullRequests().schedule();
    }
}
