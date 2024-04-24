package be.xplore.githubmetrics.domain.exports;

import be.xplore.githubmetrics.domain.domain.WorkflowRun;

import java.util.Map;

public interface WorkflowRunsExportPort {
    void exportWorkflowRunsStatusCounts(Map<WorkflowRun.RunStatus, Integer> statuses);
}
