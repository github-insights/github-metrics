package be.xplore.githubmetrics.domain.providers.ports;

import be.xplore.githubmetrics.domain.domain.Repository;
import be.xplore.githubmetrics.domain.domain.WorkflowRun;

import java.util.List;

public interface WorkflowRunsProvider {

    List<WorkflowRun> getLastDaysWorkflowRuns(Repository repository);
}
