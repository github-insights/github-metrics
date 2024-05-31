package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.apistate.ApiRateLimitState;
import be.xplore.githubmetrics.domain.workflowrun.WorkflowRun;
import be.xplore.githubmetrics.domain.workflowrun.WorkflowRunBuildTimesQueryPort;
import be.xplore.githubmetrics.domain.workflowrun.WorkflowRunStatus;
import be.xplore.githubmetrics.githubadapter.cacheevicting.CacheEvictionProperties;
import be.xplore.githubmetrics.githubadapter.cacheevicting.ScheduledCacheEvictionPort;
import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import be.xplore.githubmetrics.githubadapter.config.GithubRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class WorkflowRunBuildTimesAdapter implements WorkflowRunBuildTimesQueryPort, ScheduledCacheEvictionPort {

    private static final String WORKFLOW_RUN_BUILD_TIMES_CACHE_NAME = "WorkflowRunBuildTimes";
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowRunBuildTimesAdapter.class);
    private final GithubProperties githubProperties;
    private final GithubRestClient restClient;
    private final CacheEvictionProperties evictionProperties;
    private final ApiRateLimitState rateLimitState;
    private final GithubApiUtilities utilities;

    public WorkflowRunBuildTimesAdapter(
            GithubProperties githubProperties,
            GithubRestClient githubRestClient,
            CacheEvictionProperties evictionProperties,
            ApiRateLimitState rateLimitState,
            GithubApiUtilities utilities
    ) {
        this.githubProperties = githubProperties;
        this.restClient = githubRestClient;
        this.evictionProperties = evictionProperties;
        this.rateLimitState = rateLimitState;
        this.utilities = utilities;
    }

    @Cacheable(WORKFLOW_RUN_BUILD_TIMES_CACHE_NAME)
    @Override
    public int getWorkflowRunBuildTimes(WorkflowRun workflowRun) {
        LOGGER.debug(
                "Fetching fresh BuildTimes for WorkflowRun {} {}.",
                workflowRun.getId(), workflowRun.getName()
        );

        if (workflowRun.getStatus().equals(WorkflowRunStatus.IN_PROGRESS) ||
                workflowRun.getStatus().equals(WorkflowRunStatus.PENDING)) {
            LOGGER.debug("Workflow Run {} is in progress or pending so has no build time", workflowRun.getId());
            return 0;
        }

        var buildTime = Objects.requireNonNull(
                this.restClient.getWorkflowRunBuildTime(
                                githubProperties.org(),
                                workflowRun.getRepository().getName(),
                                String.valueOf(workflowRun.getId()))
                        .getBody()).run_duration_ms();

        LOGGER.debug(
                "Response for the BuildTimes fetch of WorkflowRun {} returned BuildTime of {}",
                workflowRun.getId(),
                buildTime
        );

        return buildTime;
    }

    @Override
    public boolean freshDataCanWait() {
        return this.rateLimitState.shouldDataWait(
                this.evictionProperties.workflowRunBuildTimes().status()
        );
    }

    @Override
    public String cacheName() {
        return WORKFLOW_RUN_BUILD_TIMES_CACHE_NAME;
    }

    @Override
    public String cronExpression() {
        return evictionProperties.workflowRunBuildTimes().schedule();
    }
}
