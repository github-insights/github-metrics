package be.xplore.githubmetrics.domain.job;

import be.xplore.githubmetrics.domain.job.model.Job;
import be.xplore.githubmetrics.domain.workflowrun.model.WorkflowRun;

import java.util.List;

public interface JobsQueryPort {
    List<Job> getAllJobsForWorkflowRun(WorkflowRun workflowRun);
}
