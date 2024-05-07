package be.xplore.githubmetrics.githubadapter.mappingclasses;

import be.xplore.githubmetrics.domain.repository.Repository;
import be.xplore.githubmetrics.domain.workflowrun.model.WorkflowRun;

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