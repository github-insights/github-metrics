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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private String getWorkflowRunsApiPath(String repoName) {
        return MessageFormat.format(
                "repos/{0}/{1}/actions/runs",
                this.githubProperties.org(),
                repoName
        );
    }

    @Override
    @Cacheable(ACTIVE_WORKFLOW_RUNS_CACHE_NAME)
    public List<WorkflowRun> getActiveWorkflowRuns(Repository repository) {
        LOGGER.info("Fetching fresh Active WorkflowRuns for Repository {}.", repository.getId());
        var parameters = getParameterMap();

        ResponseEntity<GHActionRuns> responseEntity = this.restClient.get()
                .uri(utilities.setPathAndParameters(
                        this.getWorkflowRunsApiPath(repository.getName()),
                        parameters
                ))
                .retrieve()
                .toEntity(GHActionRuns.class);

        var workflowRuns = this.utilities.followPaginationLink(
                responseEntity,
                actionRun -> actionRun.getWorkFlowRuns(repository),
                GHActionRuns.class
        );

        LOGGER.debug(
                "Response for the Active WorkflowRuns fetch of Repository {} returned {} WorkflowRuns.",
                repository.getId(),
                workflowRuns.size()
        );
        return workflowRuns;
    }

    private Map<String, List<String>> getParameterMap() {
        var parameters = new HashMap<String, List<String>>();
        parameters.put("per_page", List.of("100"));
        parameters.put(
                "status",
                List.of("requested", "queued", "pending", "in_progress"));

        return parameters;
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
