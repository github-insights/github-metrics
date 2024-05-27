package be.xplore.githubmetrics.domain.workflowrun;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetAllWorkflowRunBuildTimesOfLastDayUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetAllWorkflowRunBuildTimesOfLastDayUseCase.class);

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
        LOGGER.info("Exporting build times for {} workflow runs.", workflowRuns.size());
        workflowRuns.forEach(workflowRun ->
                workflowRun.setBuildTime(
                        workflowRunBuildTimesQueryPort.getWorkflowRunBuildTimes(workflowRun)
                ));
        return workflowRuns;
    }
}
