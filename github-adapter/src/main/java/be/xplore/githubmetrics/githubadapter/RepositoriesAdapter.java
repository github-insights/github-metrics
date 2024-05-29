package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.apistate.ApiRateLimitState;
import be.xplore.githubmetrics.domain.repository.RepositoriesQueryPort;
import be.xplore.githubmetrics.domain.repository.Repository;
import be.xplore.githubmetrics.githubadapter.cacheevicting.CacheEvictionProperties;
import be.xplore.githubmetrics.githubadapter.cacheevicting.ScheduledCacheEvictionPort;
import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import be.xplore.githubmetrics.githubadapter.mappingclasses.GHRepositories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;

@Service
public class RepositoriesAdapter implements RepositoriesQueryPort, ScheduledCacheEvictionPort {
    private static final String REPOSITORIES_CACHE_NAME = "Repositories";

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoriesAdapter.class);

    private final RestClient restClient;
    private final GithubProperties config;
    private final GithubApiUtilities utilities;
    private final CacheEvictionProperties evictionProperties;
    private final ApiRateLimitState rateLimitState;

    public RepositoriesAdapter(
            GithubProperties config,
            @Qualifier("defaultRestClient") RestClient restClient,
            GithubApiUtilities utilities,
            CacheEvictionProperties evictionProperties,
            ApiRateLimitState rateLimitState
    ) {
        this.restClient = restClient;
        this.config = config;
        this.utilities = utilities;
        this.evictionProperties = evictionProperties;
        this.rateLimitState = rateLimitState;
    }

    @Cacheable(REPOSITORIES_CACHE_NAME)
    @Override
    public List<Repository> getAllRepositories() {
        LOGGER.debug("Fetching fresh Repositories.");
        var parameters = new HashMap<String, String>();
        parameters.put("per_page", "100");

        ResponseEntity<GHRepositories> responseEntity = this.restClient.get()
                .uri(utilities.setPathAndParameters(
                        GHRepositories.PATH,
                        parameters
                ))
                .header("path", GHRepositories.PATH)
                .retrieve()
                .toEntity(GHRepositories.class);

        LOGGER.trace("Initial repositories query succeeded, about to follow pagination.");

        var repositories = this.utilities.followPaginationLink(
                responseEntity,
                GHRepositories::getRepositories,
                GHRepositories.class
        );

        LOGGER.debug(
                "Response for the Repositories fetch returned {} repositories",
                repositories.size()
        );
        return repositories;
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
