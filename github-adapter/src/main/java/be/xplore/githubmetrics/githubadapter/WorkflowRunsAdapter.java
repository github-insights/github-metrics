package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.apistate.ApiRateLimitState;
import be.xplore.githubmetrics.domain.repository.Repository;
import be.xplore.githubmetrics.domain.workflowrun.WorkflowRun;
import be.xplore.githubmetrics.domain.workflowrun.WorkflowRunsQueryPort;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

@Service
public class WorkflowRunsAdapter implements WorkflowRunsQueryPort, ScheduledCacheEvictionPort {

    private static final String WORKFLOW_RUNS_CACHE_NAME = "WorkflowRuns";
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowRunsAdapter.class);
    private final GithubProperties githubProperties;
    private final ApiRateLimitState rateLimitState;
    private final RestClient restClient;
    private final GithubApiUtilities utilities;
    private final CacheEvictionProperties evictionProperties;

    public WorkflowRunsAdapter(
            GithubProperties githubProperties,
            ApiRateLimitState rateLimitState,
            @Qualifier("defaultRestClient") RestClient restClient,
            GithubApiUtilities utilities,
            CacheEvictionProperties evictionProperties
    ) {
        this.githubProperties = githubProperties;
        this.rateLimitState = rateLimitState;
        this.restClient = restClient;
        this.utilities = utilities;
        this.evictionProperties = evictionProperties;
    }

    private String getWorkflowRunsApiPath(String repoName) {
        return MessageFormat.format(
                "repos/{0}/{1}/actions/runs",
                this.githubProperties.org(),
                repoName
        );
    }

    @Cacheable(WORKFLOW_RUNS_CACHE_NAME)
    @Override
    public List<WorkflowRun> getLastDaysWorkflowRuns(Repository repository) {
        LOGGER.debug(
                "Fetching fresh WorkflowRuns for Repository {} {}.",
                repository.getId(), repository.getName()
        );
        var parameters = new HashMap<String, String>();
        parameters.put("per_page", "100");
        parameters.put("created", ">=" + LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE));

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
                "Response for the WorkflowRuns fetch of Repository {} returned {} WorkflowRuns.",
                repository.getId(), workflowRuns.size()
        );
        return workflowRuns;
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
