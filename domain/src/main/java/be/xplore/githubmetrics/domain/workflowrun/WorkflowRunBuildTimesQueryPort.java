package be.xplore.githubmetrics.domain.workflowrun;

public interface WorkflowRunBuildTimesQueryPort {
    int getWorkflowRunBuildTimes(WorkflowRun workflowRun);
}
