package be.xplore.githubmetrics.githubadapter.mappingclasses;

import be.xplore.githubmetrics.domain.domain.WorkflowRun;

public record GHActionRun(
        long id,
        String name,
        String status,
        long workflow_id,
        String created_at,
        String updated_at
) {
    WorkflowRun getWorkFlowRun() {
        return new WorkflowRun(
                this.id,
                this.name,
                this.convertStatus()
        );
    }

    WorkflowRun.RunStatus convertStatus() {
        return switch (this.status) {
            case "completed", "success" -> WorkflowRun.RunStatus.DONE;
            case "action_required", "cancelled", "failure",
                    "neutral", "skipped", "stale", "timed_out" ->
                    WorkflowRun.RunStatus.FAILED;
            case "in_progress", "queued", "requested",
                    "waiting", "pending" -> WorkflowRun.RunStatus.PENDING;
            default ->
                    throw new IllegalStateException("Unexpected value: " + status);
        };
    }
}
