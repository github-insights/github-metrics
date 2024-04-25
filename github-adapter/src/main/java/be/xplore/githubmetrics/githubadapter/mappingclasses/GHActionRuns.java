package be.xplore.githubmetrics.githubadapter.mappingclasses;

import be.xplore.githubmetrics.domain.domain.WorkflowRun;

import java.util.List;

public record GHActionRuns(
        int total_count,
        List<GHActionRun> workflow_runs
) {
    public List<WorkflowRun> getWorkFlowRuns() {
        return this.workflow_runs.stream().map(
                GHActionRun::getWorkFlowRun
        ).toList();
    }
}