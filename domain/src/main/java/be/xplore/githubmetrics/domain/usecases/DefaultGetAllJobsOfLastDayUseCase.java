package be.xplore.githubmetrics.domain.usecases;

import be.xplore.githubmetrics.domain.domain.Job;
import be.xplore.githubmetrics.domain.domain.WorkflowRun;
import be.xplore.githubmetrics.domain.queries.JobsQueryPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultGetAllJobsOfLastDayUseCase implements GetAllJobsOfLastDayUseCase {
    private final JobsQueryPort jobsQuery;
    private final GetAllWorkflowRunsOfLastDayUseCase getAllWorkflowRunsOfLastDayUseCase;

    public DefaultGetAllJobsOfLastDayUseCase(
            JobsQueryPort jobsQuery,
            GetAllWorkflowRunsOfLastDayUseCase getAllWorkflowRunsOfLastDayUseCase) {
        this.jobsQuery = jobsQuery;
        this.getAllWorkflowRunsOfLastDayUseCase = getAllWorkflowRunsOfLastDayUseCase;
    }

    @Override
    public List<Job> getAllJobsOfLastDay() {
        List<WorkflowRun> workflowRuns
                = getAllWorkflowRunsOfLastDayUseCase.getAllWorkflowRunsOfLastDay();
        return workflowRuns.stream().map(
                jobsQuery::getAllJobsForWorkflowRun
        ).flatMap(List::stream).toList();
    }
}
