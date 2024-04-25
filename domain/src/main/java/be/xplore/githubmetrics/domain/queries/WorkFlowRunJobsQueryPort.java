package be.xplore.githubmetrics.domain.queries;

import be.xplore.githubmetrics.domain.domain.Job;

import java.util.List;

public interface WorkFlowRunJobsQueryPort {
    List<Job> getWorkFlowRunJobs(String repositoryName, long workflowRunId);
}
