package be.xplore.githubmetrics.domain.queries;

import be.xplore.githubmetrics.domain.domain.Job;
import be.xplore.githubmetrics.domain.domain.WorkflowRun;

import java.util.List;

public interface JobsQueryPort {
    List<Job> getAllJobsForWorkflowRun(WorkflowRun workflowRun);
}
