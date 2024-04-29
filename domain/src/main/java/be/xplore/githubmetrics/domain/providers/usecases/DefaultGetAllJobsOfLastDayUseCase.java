package be.xplore.githubmetrics.domain.providers.usecases;

import be.xplore.githubmetrics.domain.domain.Job;
import be.xplore.githubmetrics.domain.domain.WorkflowRun;
import be.xplore.githubmetrics.domain.providers.ports.JobsProvider;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultGetAllJobsOfLastDayUseCase implements GetAllJobsOfLastDayUseCase {
    private final JobsProvider jobsProvider;
    private final GetAllWorkflowRunsOfLastDayUseCase getAllWorkflowRunsOfLastDayUseCase;

    public DefaultGetAllJobsOfLastDayUseCase(JobsProvider jobsProvider, GetAllWorkflowRunsOfLastDayUseCase getAllWorkflowRunsOfLastDayUseCase) {
        this.jobsProvider = jobsProvider;
        this.getAllWorkflowRunsOfLastDayUseCase = getAllWorkflowRunsOfLastDayUseCase;
    }

    @Override
    public List<Job> getAllJobsOfLastDay() {
        List<WorkflowRun> workflowRuns
                = getAllWorkflowRunsOfLastDayUseCase.getAllWorkflowRunsOfLastDay();
        return workflowRuns.stream().map(
                jobsProvider::getAllJobsForWorkflowRun
        ).flatMap(List::stream).toList();
    }
}
