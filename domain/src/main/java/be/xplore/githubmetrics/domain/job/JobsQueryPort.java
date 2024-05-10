package be.xplore.githubmetrics.domain.job;

import be.xplore.githubmetrics.domain.workflowrun.WorkflowRun;

import java.util.List;

public interface JobsQueryPort {
    List<Job> getAllJobsForWorkflowRun(WorkflowRun workflowRun);
}
