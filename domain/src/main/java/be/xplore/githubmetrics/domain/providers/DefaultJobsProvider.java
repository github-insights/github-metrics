package be.xplore.githubmetrics.domain.providers;

import be.xplore.githubmetrics.domain.domain.Job;
import be.xplore.githubmetrics.domain.domain.WorkflowRun;
import be.xplore.githubmetrics.domain.providers.ports.JobsProvider;
import be.xplore.githubmetrics.domain.queries.WorkFlowRunJobsQueryPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultJobsProvider implements JobsProvider {

    private final WorkFlowRunJobsQueryPort workFlowRunJobsQueryPort;

    public DefaultJobsProvider(WorkFlowRunJobsQueryPort workFlowRunJobsQueryPort) {
        this.workFlowRunJobsQueryPort = workFlowRunJobsQueryPort;
    }

    @Override
    public List<Job> getAllJobsForWorkflowRun(WorkflowRun workflowRun) {
        return workFlowRunJobsQueryPort.getWorkFlowRunJobs(
                workflowRun.getRepository().getName(),
                workflowRun.getId()
        );
    }
}
