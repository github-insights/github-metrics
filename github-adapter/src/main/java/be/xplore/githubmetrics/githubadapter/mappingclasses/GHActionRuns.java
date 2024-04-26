package be.xplore.githubmetrics.githubadapter.mappingclasses;

import be.xplore.githubmetrics.domain.domain.Repository;
import be.xplore.githubmetrics.domain.domain.WorkflowRun;

import java.util.List;

public record GHActionRuns(
        int total_count,
        List<GHActionRun> workflow_runs
) {
    public List<WorkflowRun> getWorkFlowRuns(Repository repository) {
        return this.workflow_runs.stream().map(workflowRun ->
                workflowRun.getWorkFlowRun(repository)
        ).toList();
    }
}