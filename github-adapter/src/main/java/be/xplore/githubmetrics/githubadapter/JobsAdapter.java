package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.apistate.ApiRateLimitState;
import be.xplore.githubmetrics.domain.job.Job;
import be.xplore.githubmetrics.domain.job.JobsQueryPort;
import be.xplore.githubmetrics.domain.workflowrun.WorkflowRun;
import be.xplore.githubmetrics.githubadapter.cacheevicting.CacheEvictionProperties;
import be.xplore.githubmetrics.githubadapter.cacheevicting.ScheduledCacheEvictionPort;
import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import be.xplore.githubmetrics.githubadapter.mappingclasses.GHWorkflowRunJobs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

@Component
public class JobsAdapter implements JobsQueryPort, ScheduledCacheEvictionPort {
    private static final String JOBS_CACHE_NAME = "Jobs";
    private static final Logger LOGGER = LoggerFactory.getLogger(JobsAdapter.class);

    private final GithubProperties githubProperties;
    private final RestClient restClient;
    private final GithubApiUtilities utilities;
    private final CacheEvictionProperties evictionProperties;
    private final ApiRateLimitState rateLimitState;

    public JobsAdapter(
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

    @Cacheable(JOBS_CACHE_NAME)
    @Override
    public List<Job> getAllJobsForWorkflowRun(WorkflowRun workflowRun) {
        LOGGER.debug(
                "Fetching fresh Jobs for WorkflowRun {} {}.",
                workflowRun.getId(), workflowRun.getName()
        );

        ResponseEntity<GHWorkflowRunJobs> responseEntity = this.restClient.get()
                .uri(jobsUri(workflowRun))
                .header("path", GHWorkflowRunJobs.PATH)
                .retrieve()
                .toEntity(GHWorkflowRunJobs.class);

        var jobs = this.utilities.followPaginationLink(
                responseEntity,
                GHWorkflowRunJobs::getJobs,
                GHWorkflowRunJobs.class
        );

        LOGGER.debug(
                "Response for the Jobs fetch of WorkflowRun {} returned {} jobs.",
                workflowRun.getId(),
                jobs.size()
        );
        return jobs;
    }

    private Function<UriBuilder, URI> jobsUri(WorkflowRun workflowRun) {
        var parameters = new HashMap<String, String>();
        parameters.put("per_page", "100");

        List<Object> pathVars = List.of(
                this.githubProperties.org(),
                workflowRun.getRepository().getName(),
                workflowRun.getId()
        );

        return utilities.setPathAndParameters(
                GHWorkflowRunJobs.PATH,
                pathVars, parameters
        );
    }

    @Override
    public boolean freshDataCanWait() {
        return this.rateLimitState.shouldDataWait(
                this.evictionProperties.jobs().status()
        );
    }

    @Override
    public String cacheName() {
        return JOBS_CACHE_NAME;
    }

    @Override
    public String cronExpression() {
        return this.evictionProperties.jobs().schedule();
    }
}
