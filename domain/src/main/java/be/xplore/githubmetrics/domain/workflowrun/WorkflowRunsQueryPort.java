package be.xplore.githubmetrics.domain.workflowrun;

import be.xplore.githubmetrics.domain.repository.Repository;

import java.util.List;

public interface WorkflowRunsQueryPort {
    List<WorkflowRun> getLastDaysWorkflowRuns(Repository repository);

}
