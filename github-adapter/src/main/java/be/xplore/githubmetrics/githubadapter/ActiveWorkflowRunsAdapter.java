package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.apistate.ApiRateLimitState;
import be.xplore.githubmetrics.domain.repository.Repository;
import be.xplore.githubmetrics.domain.workflowrun.ActiveWorkflowRunsQueryPort;
import be.xplore.githubmetrics.domain.workflowrun.WorkflowRun;
import be.xplore.githubmetrics.githubadapter.cacheevicting.CacheEvictionProperties;
import be.xplore.githubmetrics.githubadapter.cacheevicting.ScheduledCacheEvictionPort;
import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import be.xplore.githubmetrics.githubadapter.config.GithubRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Service
public class ActiveWorkflowRunsAdapter implements ActiveWorkflowRunsQueryPort, ScheduledCacheEvictionPort {

    private static final String ACTIVE_WORKFLOW_RUNS_CACHE_NAME = "ActiveWorkflowRuns";

    private static final Logger LOGGER = LoggerFactory.getLogger(ActiveWorkflowRunsAdapter.class);
    private final GithubProperties githubProperties;
    private final GithubRestClient restClient;
    private final CacheEvictionProperties evictionProperties;
    private final ApiRateLimitState rateLimitState;

    public ActiveWorkflowRunsAdapter(
            GithubProperties githubProperties,
            GithubRestClient githubRestClient,
            CacheEvictionProperties evictionProperties,
            ApiRateLimitState rateLimitState
    ) {
        this.githubProperties = githubProperties;
        this.restClient = githubRestClient;
        this.evictionProperties = evictionProperties;
        this.rateLimitState = rateLimitState;
    }

    @Override
    @Cacheable(ACTIVE_WORKFLOW_RUNS_CACHE_NAME)
    public List<WorkflowRun> getActiveWorkflowRuns(Repository repository) {
        LOGGER.info("Fetching fresh Active WorkflowRuns for Repository {}.", repository.getId());

        var parameters = new HashMap<String, String>();
        parameters.put("per_page", "100");

        var workflowRuns = Objects.requireNonNull(
                this.restClient.getWorkflowRuns(
                                githubProperties.org(),
                                repository.getName(),
                                parameters)
                        .getBody()).getActiveWorkflowRuns(repository);

        LOGGER.debug(
                "Response for the Active WorkflowRuns fetch of Repository {} returned {} WorkflowRuns.",
                repository.getId(),
                workflowRuns.size()
        );
        return workflowRuns;
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
