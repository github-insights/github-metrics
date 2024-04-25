package be.xplore.githubmetrics.domain.queries;

import be.xplore.githubmetrics.domain.domain.WorkflowRun;
import be.xplore.githubmetrics.domain.exceptions.GenericAdapterException;

import java.util.List;

public interface WorkflowRunsQueryPort {
    List<WorkflowRun> getLastDaysWorkflows(String repositoryName) throws GenericAdapterException;

}