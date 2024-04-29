package be.xplore.githubmetrics.domain.providers.ports;

import be.xplore.githubmetrics.domain.domain.Job;
import be.xplore.githubmetrics.domain.domain.WorkflowRun;

import java.util.List;

public interface JobsProvider {
    List<Job> getAllJobsForWorkflowRun(WorkflowRun workflowRun);
}
