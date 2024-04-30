package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.domain.Job;
import be.xplore.githubmetrics.domain.queries.WorkFlowRunJobsQueryPort;
import be.xplore.githubmetrics.githubadapter.mappingclasses.GHWorkflowRunJobs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;

@Component
public class WorkflowRunJobsAdapter implements WorkFlowRunJobsQueryPort {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowRunJobsAdapter.class);
    private final GithubAdapter githubAdapter;

    public WorkflowRunJobsAdapter(GithubAdapter githubAdapter) {
        this.githubAdapter = githubAdapter;
    }

    @Override
    public List<Job> getWorkFlowRunJobs(String repositoryName, long workflowRunId) {
        LOGGER.debug("Getting Jobs for workflow run: {}", workflowRunId);
        var responseSpec = GithubAdapter.getBody(
                githubAdapter.getResponseSpec(
                        MessageFormat.format(
                                "repos/{0}/{1}/actions/runs/{2,number,#}/jobs",
                                this.githubAdapter.getConfig().org(),
                                repositoryName,
                                workflowRunId
                        ),
                        new HashMap<>()
                ),
                GHWorkflowRunJobs.class
        );

        List<Job> jobs = responseSpec.getJobs();
        LOGGER.debug("number of jobs for workflow run {}: {}", workflowRunId, jobs.size());
        return jobs;
    }
}
