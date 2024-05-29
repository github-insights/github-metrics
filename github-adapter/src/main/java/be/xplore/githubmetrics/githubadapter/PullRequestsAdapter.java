package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.apistate.ApiRateLimitState;
import be.xplore.githubmetrics.domain.pullrequest.PullRequest;
import be.xplore.githubmetrics.domain.pullrequest.PullRequestQueryPort;
import be.xplore.githubmetrics.domain.repository.Repository;
import be.xplore.githubmetrics.githubadapter.cacheevicting.CacheEvictionProperties;
import be.xplore.githubmetrics.githubadapter.cacheevicting.ScheduledCacheEvictionPort;
import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import be.xplore.githubmetrics.githubadapter.mappingclasses.GHPullRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

@Service
public class PullRequestsAdapter implements PullRequestQueryPort, ScheduledCacheEvictionPort {
    private static final String PULL_REQUESTS_CACHE_NAME = "PullRequests";
    private static final Logger LOGGER = LoggerFactory.getLogger(PullRequestsAdapter.class);

    private final GithubProperties githubProperties;
    private final RestClient restClient;
    private final GithubApiUtilities utilities;
    private final CacheEvictionProperties evictionProperties;
    private final ApiRateLimitState rateLimitState;

    public PullRequestsAdapter(
            GithubProperties githubProperties,
            @Qualifier("defaultRestClient") RestClient restClient,
            GithubApiUtilities utilities,
            CacheEvictionProperties evictionProperties,
            ApiRateLimitState rateLimitState
    ) {
        this.githubProperties = githubProperties;
        this.restClient = restClient;
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

        ResponseEntity<GHPullRequest[]> responseEntity = this.restClient.get()
                .uri(pullRequestsUri(repository))
                .header("path", GHPullRequest.PATH_ALL)
                .retrieve()
                .toEntity(GHPullRequest[].class);

        var pullRequests = this.utilities.followPaginationLink(
                responseEntity,
                ghPullRequests -> Arrays.stream(ghPullRequests).map(GHPullRequest::getPullRequest).toList(),
                GHPullRequest[].class
        );

        LOGGER.debug(
                "Response for the PullRequests fetch of Repository {} returned {} PullRequests.",
                repository.getId(),
                pullRequests.size()
        );

        return pullRequests;
    }

    private Function<UriBuilder, URI> pullRequestsUri(Repository repository) {
        List<Object> pathVars = List.of(
                this.githubProperties.org(), repository.getName()
        );
        var parameters = new HashMap<String, String>();
        parameters.put("state", "all");
        parameters.put("per_page", "100");

        return utilities.setPathAndParameters(
                GHPullRequest.PATH_ALL,
                pathVars, parameters
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
