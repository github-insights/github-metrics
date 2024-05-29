package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.apistate.ApiRateLimitState;
import be.xplore.githubmetrics.domain.workflowrun.WorkflowRun;
import be.xplore.githubmetrics.domain.workflowrun.WorkflowRunBuildTimesQueryPort;
import be.xplore.githubmetrics.domain.workflowrun.WorkflowRunStatus;
import be.xplore.githubmetrics.githubadapter.cacheevicting.CacheEvictionProperties;
import be.xplore.githubmetrics.githubadapter.cacheevicting.ScheduledCacheEvictionPort;
import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import be.xplore.githubmetrics.githubadapter.mappingclasses.GHActionRunTiming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Service
public class WorkflowRunBuildTimesAdapter implements WorkflowRunBuildTimesQueryPort, ScheduledCacheEvictionPort {

    private static final String WORKFLOW_RUN_BUILD_TIMES_CACHE_NAME = "WorkflowRunBuildTimes";
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowRunBuildTimesAdapter.class);
    private final GithubProperties githubProperties;
    private final RestClient restClient;
    private final CacheEvictionProperties evictionProperties;
    private final ApiRateLimitState rateLimitState;
    private final GithubApiUtilities utilities;

    public WorkflowRunBuildTimesAdapter(
            GithubProperties githubProperties,
            @Qualifier("defaultRestClient") RestClient restClient,
            CacheEvictionProperties evictionProperties,
            ApiRateLimitState rateLimitState,
            GithubApiUtilities utilities
    ) {
        this.githubProperties = githubProperties;
        this.restClient = restClient;
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
                        this.restClient.get()
                                .uri(buildTimesUri(workflowRun))
                                .header("path", GHActionRunTiming.PATH)
                                .retrieve()
                                .body(GHActionRunTiming.class)
                )
                .run_duration_ms();

        LOGGER.debug(
                "Response for the BuildTimes fetch of WorkflowRun {} returned BuildTime of {}",
                workflowRun.getId(),
                buildTime
        );

        return buildTime;
    }

    private Function<UriBuilder, URI> buildTimesUri(WorkflowRun workflowRun) {
        List<Object> uriVars = List.of(
                this.githubProperties.org(),
                workflowRun.getRepository().getName(),
                workflowRun.getId()
        );
        return this.utilities.setPathAndParameters(
                GHActionRunTiming.PATH,
                uriVars
        );
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
