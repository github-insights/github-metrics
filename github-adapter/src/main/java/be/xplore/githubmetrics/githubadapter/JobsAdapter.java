package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.apistate.ApiRateLimitState;
import be.xplore.githubmetrics.domain.job.Job;
import be.xplore.githubmetrics.domain.job.JobsQueryPort;
import be.xplore.githubmetrics.domain.workflowrun.WorkflowRun;
import be.xplore.githubmetrics.githubadapter.cacheevicting.CacheEvictionProperties;
import be.xplore.githubmetrics.githubadapter.cacheevicting.ScheduledCacheEvictionPort;
import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import be.xplore.githubmetrics.githubadapter.config.GithubRestClient;
import be.xplore.githubmetrics.githubadapter.mappingclasses.GHWorkflowRunJobs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JobsAdapter implements JobsQueryPort, ScheduledCacheEvictionPort {
    private static final String JOBS_CACHE_NAME = "Jobs";
    private static final Logger LOGGER = LoggerFactory.getLogger(JobsAdapter.class);

    private final GithubProperties githubProperties;
    private final GithubRestClient restClient;
    private final GithubApiUtilities utilities;
    private final CacheEvictionProperties evictionProperties;
    private final ApiRateLimitState rateLimitState;

    public JobsAdapter(
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

    @Cacheable(JOBS_CACHE_NAME)
    @Override
    public List<Job> getAllJobsForWorkflowRun(WorkflowRun workflowRun) {
        LOGGER.debug(
                "Fetching fresh Jobs for WorkflowRun {} {}.",
                workflowRun.getId(), workflowRun.getName()
        );
        var parameters = new HashMap<String, String>();
        parameters.put("per_page", "100");
        parameters.put("page", "0");

        var jobs = makeRequestAndFollowPagination(parameters, workflowRun);

        LOGGER.debug(
                "Response for the Jobs fetch of WorkflowRun {} returned {} jobs.",
                workflowRun.getId(),
                jobs.size()
        );
        return jobs;
    }

    private List<Job> makeRequestAndFollowPagination(
            Map<String, String> parameters,
            WorkflowRun workflowRun
    ) {
        ResponseEntity<GHWorkflowRunJobs> responseEntity
                = this.restClient.getJobs(
                githubProperties.org(),
                workflowRun.getRepository().getName(),
                String.valueOf(workflowRun.getId()),
                parameters
        );

        return this.utilities.followPaginationLink(
                responseEntity,
                1,
                pageNumber -> {
                    parameters.put("page", String.valueOf(pageNumber));
                    return this.restClient.getJobs(
                            githubProperties.org(),
                            workflowRun.getRepository().getName(),
                            String.valueOf(workflowRun.getId()),
                            parameters);
                },
                GHWorkflowRunJobs::getJobs
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
