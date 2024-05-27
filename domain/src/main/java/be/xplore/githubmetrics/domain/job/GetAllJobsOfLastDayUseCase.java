package be.xplore.githubmetrics.domain.job;

import be.xplore.githubmetrics.domain.workflowrun.GetAllWorkflowRunsOfLastDayUseCase;
import be.xplore.githubmetrics.domain.workflowrun.WorkflowRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetAllJobsOfLastDayUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetAllJobsOfLastDayUseCase.class);

    private final JobsQueryPort jobsQuery;
    private final GetAllWorkflowRunsOfLastDayUseCase getAllWorkflowRunsOfLastDayUseCase;

    public GetAllJobsOfLastDayUseCase(
            JobsQueryPort jobsQuery,
            GetAllWorkflowRunsOfLastDayUseCase getAllWorkflowRunsOfLastDayUseCase) {
        this.jobsQuery = jobsQuery;
        this.getAllWorkflowRunsOfLastDayUseCase = getAllWorkflowRunsOfLastDayUseCase;
    }

    public List<Job> getAllJobsOfLastDay() {
        List<WorkflowRun> workflowRuns
                = getAllWorkflowRunsOfLastDayUseCase.getAllWorkflowRunsOfLastDay();
        LOGGER.info("Exporting Jobs of {} workflow runs.", workflowRuns.size());
        return workflowRuns.stream().map(
                jobsQuery::getAllJobsForWorkflowRun
        ).flatMap(List::stream).toList();
    }
}
