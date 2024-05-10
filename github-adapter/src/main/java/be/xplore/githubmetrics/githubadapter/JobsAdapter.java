package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.job.JobsQueryPort;
import be.xplore.githubmetrics.domain.job.model.Job;
import be.xplore.githubmetrics.domain.workflowrun.model.WorkflowRun;
import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import be.xplore.githubmetrics.githubadapter.mappingclasses.GHWorkflowRunJobs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.text.MessageFormat;
import java.util.List;

@Component
public class JobsAdapter implements JobsQueryPort {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobsAdapter.class);
    private final GithubProperties githubProperties;
    private final RestClient restClient;

    public JobsAdapter(
            GithubProperties githubProperties,
            @Qualifier("defaultRestClient") RestClient restClient
    ) {
        this.githubProperties = githubProperties;
        this.restClient = restClient;
    }

    private String getJobsApiPath(String repoName, long workflowRunId) {
        return MessageFormat.format(
                "repos/{0}/{1}/actions/runs/{2,number,#}/jobs",
                this.githubProperties.org(),
                repoName,
                workflowRunId
        );
    }

    @Override
    public List<Job> getAllJobsForWorkflowRun(WorkflowRun workflowRun) {
        LOGGER.debug("Getting Jobs for workflow run: {}", workflowRun.getId());
        var response = this.restClient.get()
                .uri(getJobsApiPath(workflowRun.getRepository().getName(), workflowRun.getId()))
                .retrieve()
                .body(GHWorkflowRunJobs.class);

        List<Job> jobs = response.getJobs();
        LOGGER.debug("number of jobs for workflow run {}: {}", workflowRun.getId(), jobs.size());
        return jobs;
    }
}
