package be.xplore.githubmetrics.domain.providers;

import be.xplore.githubmetrics.domain.domain.Repository;
import be.xplore.githubmetrics.domain.domain.WorkflowRun;
import be.xplore.githubmetrics.domain.providers.ports.WorkflowRunsProvider;
import be.xplore.githubmetrics.domain.queries.WorkflowRunsQueryPort;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CachedWorkflowRunsProvider implements WorkflowRunsProvider {

    private final WorkflowRunsQueryPort workflowRunsQueryPort;

    public CachedWorkflowRunsProvider(WorkflowRunsQueryPort workflowRunsQueryPort) {
        this.workflowRunsQueryPort = workflowRunsQueryPort;
    }

    @Override
    @Cacheable("workflowruns.lastday")
    public List<WorkflowRun> getLastDaysWorkflowRuns(Repository repository) {
        return workflowRunsQueryPort.getLastDaysWorkflows(repository);
    }
}
