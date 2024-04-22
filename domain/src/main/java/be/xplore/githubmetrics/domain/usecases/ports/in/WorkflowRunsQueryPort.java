package be.xplore.githubmetrics.domain.usecases.ports.in;

import be.xplore.githubmetrics.domain.domain.WorkflowRun;

import java.util.List;

public interface WorkflowRunsQueryPort {
    List<WorkflowRun> getLastDaysWorkflows();
}
