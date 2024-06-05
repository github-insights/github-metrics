package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.apistate.ApiRateLimitState;
import be.xplore.githubmetrics.domain.repository.Repository;
import be.xplore.githubmetrics.domain.workflowrun.WorkflowRun;
import be.xplore.githubmetrics.domain.workflowrun.WorkflowRunsQueryPort;
import be.xplore.githubmetrics.githubadapter.cacheevicting.CacheEvictionProperties;
import be.xplore.githubmetrics.githubadapter.cacheevicting.ScheduledCacheEvictionPort;
import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import be.xplore.githubmetrics.githubadapter.config.GithubRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WorkflowRunsAdapter implements WorkflowRunsQueryPort, ScheduledCacheEvictionPort {

    private static final String WORKFLOW_RUNS_CACHE_NAME = "WorkflowRuns";
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowRunsAdapter.class);
    private final GithubProperties githubProperties;
    private final ApiRateLimitState rateLimitState;
    private final GithubRestClient restClient;
    private final GithubApiUtilities utilities;
    private final CacheEvictionProperties evictionProperties;

    public WorkflowRunsAdapter(
            GithubProperties githubProperties,
            ApiRateLimitState rateLimitState,
            GithubRestClient githubRestClient,
            GithubApiUtilities utilities,
            CacheEvictionProperties evictionProperties
    ) {
        this.githubProperties = githubProperties;
        this.rateLimitState = rateLimitState;
        this.restClient = githubRestClient;
        this.utilities = utilities;
        this.evictionProperties = evictionProperties;
    }

    @Cacheable(WORKFLOW_RUNS_CACHE_NAME)
    @Override
    public List<WorkflowRun> getLastDaysWorkflowRuns(Repository repository) {
        LOGGER.debug(
                "Fetching fresh WorkflowRuns for Repository {} {}.",
                repository.getId(), repository.getName()
        );

        var workflowRuns = this.makeRequestAndFollowPagination(
                this.getParameters(), repository);

        LOGGER.debug(
                "Response for the WorkflowRuns fetch of Repository {} returned {} WorkflowRuns.",
                repository.getId(), workflowRuns.size()
        );
        return workflowRuns;
    }

    private Map<String, String> getParameters() {
        var parameters = new HashMap<String, String>();
        parameters.put("per_page", "100");
        parameters.put("page", "0");
        parameters.put("created", ">=" + OffsetDateTime.now().minusDays(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        return parameters;
    }

    private List<WorkflowRun> makeRequestAndFollowPagination(Map<String, String> parameters, Repository repository) {
        var responseEntity = this.restClient.getWorkflowRuns(
                githubProperties.org(),
                repository.getName(),
                parameters
        );

        return this.utilities.followPaginationLink(
                responseEntity,
                1,
                pageNumber -> {
                    parameters.put("page", String.valueOf(pageNumber));
                    return this.restClient.getWorkflowRuns(
                            githubProperties.org(),
                            repository.getName(),
                            parameters);
                },
                ghActionRuns -> ghActionRuns.getWorkFlowRuns(repository)
        );
    }

    @Override
    public boolean freshDataCanWait() {
        return this.rateLimitState.shouldDataWait(
                this.evictionProperties.workflowRuns().status()
        );
    }

    @Override
    public String cacheName() {
        return WORKFLOW_RUNS_CACHE_NAME;
    }

    @Override
    public String cronExpression() {
        return evictionProperties.workflowRuns().schedule();
    }
}
