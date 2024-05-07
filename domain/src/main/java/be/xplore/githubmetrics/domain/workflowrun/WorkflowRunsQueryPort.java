package be.xplore.githubmetrics.domain.workflowrun;

import be.xplore.githubmetrics.domain.exceptions.GenericAdapterException;
import be.xplore.githubmetrics.domain.repository.Repository;
import be.xplore.githubmetrics.domain.workflowrun.model.WorkflowRun;

import java.util.List;

public interface WorkflowRunsQueryPort {
    List<WorkflowRun> getLastDaysWorkflowRuns(Repository repository) throws GenericAdapterException;

}
