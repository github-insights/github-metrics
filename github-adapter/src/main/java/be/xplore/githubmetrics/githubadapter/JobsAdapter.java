package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.job.JobsQueryPort;
import be.xplore.githubmetrics.domain.job.model.Job;
import be.xplore.githubmetrics.domain.workflowrun.model.WorkflowRun;
import be.xplore.githubmetrics.githubadapter.mappingclasses.GHWorkflowRunJobs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;

@Component
public class JobsAdapter implements JobsQueryPort {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobsAdapter.class);
    private final GithubAdapter githubAdapter;

    public JobsAdapter(GithubAdapter githubAdapter) {
        this.githubAdapter = githubAdapter;
    }

    @Override
    public List<Job> getAllJobsForWorkflowRun(WorkflowRun workflowRun) {
        LOGGER.debug("Getting Jobs for workflow run: {}", workflowRun.getId());
        var responseSpec = GithubAdapter.getBody(
                githubAdapter.getResponseSpec(
                        MessageFormat.format(
                                "repos/{0}/{1}/actions/runs/{2,number,#}/jobs",
                                this.githubAdapter.getConfig().org(),
                                workflowRun.getRepository().getName(),
                                workflowRun.getId()
                        ),
                        new HashMap<>()
                ),
                GHWorkflowRunJobs.class
        );

        List<Job> jobs = responseSpec.getJobs();
        LOGGER.debug("number of jobs for workflow run {}: {}", workflowRun.getId(), jobs.size());
        return jobs;
    }
}
