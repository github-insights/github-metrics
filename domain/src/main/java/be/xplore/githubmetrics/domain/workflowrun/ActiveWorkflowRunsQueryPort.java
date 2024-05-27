package be.xplore.githubmetrics.domain.workflowrun;

import be.xplore.githubmetrics.domain.repository.Repository;

import java.util.List;

public interface ActiveWorkflowRunsQueryPort {
    List<WorkflowRun> getActiveWorkflowRuns(Repository repository);
}
