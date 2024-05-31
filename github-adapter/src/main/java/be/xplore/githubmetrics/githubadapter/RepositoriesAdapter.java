package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.apistate.ApiRateLimitState;
import be.xplore.githubmetrics.domain.repository.RepositoriesQueryPort;
import be.xplore.githubmetrics.domain.repository.Repository;
import be.xplore.githubmetrics.githubadapter.cacheevicting.CacheEvictionProperties;
import be.xplore.githubmetrics.githubadapter.cacheevicting.ScheduledCacheEvictionPort;
import be.xplore.githubmetrics.githubadapter.config.GithubRestClient;
import be.xplore.githubmetrics.githubadapter.mappingclasses.GHRepositories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RepositoriesAdapter implements RepositoriesQueryPort, ScheduledCacheEvictionPort {
    private static final String REPOSITORIES_CACHE_NAME = "Repositories";

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoriesAdapter.class);

    private final GithubRestClient restClient;
    private final GithubApiUtilities utilities;
    private final CacheEvictionProperties evictionProperties;
    private final ApiRateLimitState rateLimitState;

    public RepositoriesAdapter(
            GithubRestClient githubRestClient,
            GithubApiUtilities utilities,
            CacheEvictionProperties evictionProperties,
            ApiRateLimitState rateLimitState
    ) {
        this.restClient = githubRestClient;
        this.utilities = utilities;
        this.evictionProperties = evictionProperties;
        this.rateLimitState = rateLimitState;
    }

    @Cacheable(REPOSITORIES_CACHE_NAME)
    @Override
    public List<Repository> getAllRepositories() {
        LOGGER.debug("Fetching fresh Repositories.");
        var parameters = new HashMap<String, String>();
        parameters.put("page", "0");
        parameters.put("per_page", "100");

        var repositories = this.makeRequestAndFollowPagination(parameters);

        LOGGER.debug(
                "Response for the Repositories fetch returned {} repositories",
                repositories.size()
        );
        return repositories;
    }

    private List<Repository> makeRequestAndFollowPagination(Map<String, String> parameters) {
        ResponseEntity<GHRepositories> responseEntity
                = this.restClient.getRepositories(parameters);

        return this.utilities.followPaginationLink(
                responseEntity,
                1,
                pageNumber -> {
                    parameters.put("page", String.valueOf(pageNumber));
                    return this.restClient.getRepositories(parameters);
                },
                GHRepositories::getRepositories
        );
    }

    @Override
    public boolean freshDataCanWait() {
        return this.rateLimitState.shouldDataWait(
                this.evictionProperties.repositoryCount().status()
        );
    }

    @Override
    public String cacheName() {
        return REPOSITORIES_CACHE_NAME;
    }

    @Override
    public String cronExpression() {
        return this.evictionProperties.repositoryCount().schedule();
    }
}
