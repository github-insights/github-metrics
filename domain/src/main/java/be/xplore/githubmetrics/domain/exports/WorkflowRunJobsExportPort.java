package be.xplore.githubmetrics.domain.exports;

import be.xplore.githubmetrics.domain.schedulers.WorkFlowRunJobsScheduler;

import java.util.Map;

public interface WorkflowRunJobsExportPort {
    void exportWorkflowRunJobsLabelsCounts(
            Map<WorkFlowRunJobsScheduler.JobLabels, Integer> statuses
    );
}
