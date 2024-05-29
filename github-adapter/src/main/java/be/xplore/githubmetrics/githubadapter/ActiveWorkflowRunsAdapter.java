package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.apistate.ApiRateLimitState;
import be.xplore.githubmetrics.domain.repository.Repository;
import be.xplore.githubmetrics.domain.workflowrun.ActiveWorkflowRunsQueryPort;
import be.xplore.githubmetrics.domain.workflowrun.WorkflowRun;
import be.xplore.githubmetrics.githubadapter.cacheevicting.CacheEvictionProperties;
import be.xplore.githubmetrics.githubadapter.cacheevicting.ScheduledCacheEvictionPort;
import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import be.xplore.githubmetrics.githubadapter.mappingclasses.GHActionRuns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Service
public class ActiveWorkflowRunsAdapter implements ActiveWorkflowRunsQueryPort, ScheduledCacheEvictionPort {

    private static final String ACTIVE_WORKFLOW_RUNS_CACHE_NAME = "ActiveWorkflowRuns";

    private static final Logger LOGGER = LoggerFactory.getLogger(ActiveWorkflowRunsAdapter.class);
    private final GithubProperties githubProperties;
    private final RestClient restClient;
    private final GithubApiUtilities utilities;

    private final CacheEvictionProperties evictionProperties;
    private final ApiRateLimitState rateLimitState;

    public ActiveWorkflowRunsAdapter(
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

    @Override
    @Cacheable(ACTIVE_WORKFLOW_RUNS_CACHE_NAME)
    public List<WorkflowRun> getActiveWorkflowRuns(Repository repository) {
        LOGGER.info("Fetching fresh Active WorkflowRuns for Repository {}.", repository.getId());

        var workflowRuns = Objects.requireNonNull(this.restClient.get()
                .uri(workflowRunsUri(repository))
                .header("path", GHActionRuns.PATH)
                .retrieve()
                .body(GHActionRuns.class)).getActiveWorkflowRuns(repository);

        LOGGER.debug(
                "Response for the Active WorkflowRuns fetch of Repository {} returned {} WorkflowRuns.",
                repository.getId(),
                workflowRuns.size()
        );
        return workflowRuns;
    }

    private Function<UriBuilder, URI> workflowRunsUri(Repository repository) {
        var parameters = new HashMap<String, String>();
        parameters.put("per_page", "100");

        List<Object> pathVars = List.of(
                this.githubProperties.org(),
                repository.getName()
        );
        return utilities.setPathAndParameters(
                GHActionRuns.PATH,
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
        return ACTIVE_WORKFLOW_RUNS_CACHE_NAME;
    }

    @Override
    public String cronExpression() {
        return this.evictionProperties.activeWorkflowRuns().schedule();
    }
}
