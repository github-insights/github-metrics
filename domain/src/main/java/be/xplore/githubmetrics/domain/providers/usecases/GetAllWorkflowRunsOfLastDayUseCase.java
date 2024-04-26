package be.xplore.githubmetrics.domain.providers.usecases;

import be.xplore.githubmetrics.domain.domain.WorkflowRun;

import java.util.List;

public interface GetAllWorkflowRunsOfLastDayUseCase {
    List<WorkflowRun> getAllWorkflowRunsOfLastDay();
}
