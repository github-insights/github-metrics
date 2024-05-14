package be.xplore.githubmetrics.domain.workflowrun;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetAllWorkflowRunBuildTimesOfLastDayUseCase {
    private final GetAllWorkflowRunsOfLastDayUseCase getAllWorkflowRunsOfLastDayUseCase;
    private final WorkflowRunBuildTimesQueryPort workflowRunBuildTimesQueryPort;

    public GetAllWorkflowRunBuildTimesOfLastDayUseCase(
            GetAllWorkflowRunsOfLastDayUseCase getAllWorkflowRunsOfLastDayUseCase,
            WorkflowRunBuildTimesQueryPort workflowRunBuildTimesQueryPort
    ) {
        this.getAllWorkflowRunsOfLastDayUseCase = getAllWorkflowRunsOfLastDayUseCase;
        this.workflowRunBuildTimesQueryPort = workflowRunBuildTimesQueryPort;
    }

    public List<WorkflowRun> getAllWorkflowRunBuildTime() {
        List<WorkflowRun> workflowRuns
                = getAllWorkflowRunsOfLastDayUseCase.getAllWorkflowRunsOfLastDay();
        workflowRuns.forEach(workflowRun ->
                workflowRun.setBuildTime(
                        workflowRunBuildTimesQueryPort.getWorkflowRunBuildTimes(workflowRun)
                ));
        return workflowRuns;
    }
}
