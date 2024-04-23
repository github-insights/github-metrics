package be.xplore.githubmetrics.githubadapter.mappingclasses;

import be.xplore.githubmetrics.domain.domain.WorkflowRun;

import java.util.ArrayList;
import java.util.List;

public record GHActionRuns(
        int total_count,
        List<GHActionRun> workflow_runs
) {
    public List<WorkflowRun> getWorkFlowRuns() {
        List<WorkflowRun> workflowRunsList = new ArrayList<>();
        for (GHActionRun workflow_run : workflow_runs) {
            workflowRunsList.add(workflow_run.getWorkFlowRun());
        }
        return workflowRunsList;
    }
}