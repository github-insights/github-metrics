package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.apistate.ApiRateLimitState;
import be.xplore.githubmetrics.domain.repository.Repository;
import be.xplore.githubmetrics.domain.selfhostedrunner.SelfHostedRunner;
import be.xplore.githubmetrics.domain.selfhostedrunner.SelfHostedRunnersQueryPort;
import be.xplore.githubmetrics.githubadapter.cacheevicting.CacheEvictionProperties;
import be.xplore.githubmetrics.githubadapter.cacheevicting.ScheduledCacheEvictionPort;
import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import be.xplore.githubmetrics.githubadapter.config.GithubRestClient;
import be.xplore.githubmetrics.githubadapter.mappingclasses.GHSelfHostedRunners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class SelfHostedRunnerAdapter implements SelfHostedRunnersQueryPort, ScheduledCacheEvictionPort {

    private static final String SELF_HOSTED_RUNNERS_CACHE_NAME = "SelfHostedRunners";
    private static final Logger LOGGER = LoggerFactory.getLogger(SelfHostedRunnerAdapter.class);
    private final GithubProperties githubProperties;
    private final GithubRestClient restClient;
    private final GithubApiUtilities utilities;
    private final CacheEvictionProperties evictionProperties;
    private final ApiRateLimitState rateLimitState;

    public SelfHostedRunnerAdapter(
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

    @Cacheable(SELF_HOSTED_RUNNERS_CACHE_NAME)
    @Override
    public List<SelfHostedRunner> getAllSelfHostedRunners(List<Repository> repositories) {
        var organizationRunners = this.getForOrganization();
        var repositoryRunners = this.getForRepositories(repositories);
        return Stream.concat(organizationRunners.stream(), repositoryRunners.stream()).toList();
    }

    private List<SelfHostedRunner> getForOrganization() {
        LOGGER.debug("Fetching fresh SelfHostedRunners for Organization.");
        var parameters = getParameters();

        var responseEntity = this.restClient.getOrgSelfHostedRunners(
                githubProperties.org(),
                parameters
        );
        var orgSelfHostedRunners = this.utilities.followPaginationLink(
                responseEntity,
                0,
                pageNumber -> {
                    parameters.put("page", String.valueOf(pageNumber));
                    return this.restClient.getOrgSelfHostedRunners(
                            githubProperties.org(),
                            parameters
                    );
                },
                ghSelfHostedRunners -> ghSelfHostedRunners.getRunners(
                        this.githubProperties.parsing().selfHostedRunnerOsKeywords()
                )
        );
        LOGGER.debug(
                "Response for the SelfHostedRunner fetch of Organization returned {} SelfHostedRunners.",
                orgSelfHostedRunners.size()
        );

        return orgSelfHostedRunners;
    }

    private List<SelfHostedRunner> getForRepositories(List<Repository> repositories) {
        return repositories.stream().map(this::getForRepository).flatMap(List::stream).toList();
    }

    private List<SelfHostedRunner> getForRepository(Repository repository) {
        LOGGER.debug("Fetching fresh SelfHostedRunners for Repository {}.", repository.getName());
        var parameters = getParameters();
        ResponseEntity<GHSelfHostedRunners> responseEntity
                = this.restClient.getRepoSelfHostedRunners(
                githubProperties.org(),
                repository.getName(),
                this.getParameters()
        );
        var repoSelfHostedRunners = this.utilities.followPaginationLink(
                responseEntity,
                1,
                pageNumber -> {
                    parameters.put("page", String.valueOf(pageNumber));
                    return this.restClient.getRepoSelfHostedRunners(
                            githubProperties.org(),
                            repository.getName(),
                            parameters
                    );
                },
                ghSelfHostedRunners -> ghSelfHostedRunners.getRunners(
                        this.githubProperties.parsing().selfHostedRunnerOsKeywords()
                )
        );

        LOGGER.debug(
                "Response for the SelfHostedRunner fetch of Repository {} returned {} SelfHostedRunners.",
                repository.getName(), repoSelfHostedRunners.size()
        );
        return repoSelfHostedRunners;
    }

    private Map<String, String> getParameters() {
        var parameters = new HashMap<String, String>();
        parameters.put("per_page", "100");
        parameters.put("page", "0");
        return parameters;
    }

    @Override
    public boolean freshDataCanWait() {
        return this.rateLimitState.shouldDataWait(
                this.evictionProperties.selfHostedRunners().status()
        );
    }

    @Override
    public String cacheName() {
        return SELF_HOSTED_RUNNERS_CACHE_NAME;
    }

    @Override
    public String cronExpression() {
        return this.evictionProperties.selfHostedRunners().schedule();
    }
}
