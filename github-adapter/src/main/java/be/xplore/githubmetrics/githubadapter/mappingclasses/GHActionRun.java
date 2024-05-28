package be.xplore.githubmetrics.githubadapter.mappingclasses;

import be.xplore.githubmetrics.domain.repository.Repository;
import be.xplore.githubmetrics.domain.workflowrun.WorkflowRun;
import be.xplore.githubmetrics.domain.workflowrun.WorkflowRunStatus;

import java.util.List;

@SuppressWarnings("PMD.CyclomaticComplexity")
public record GHActionRun(
        long id,
        String name,
        String status,
        long workflow_id,
        String created_at,
        String updated_at
) {
    WorkflowRun getWorkFlowRun(Repository repository) {
        return new WorkflowRun(
                this.id,
                this.name,
                this.convertStatus(),
                repository
        );
    }

    WorkflowRunStatus convertStatus() {
        return switch (this.status) {
            case "completed", "success" -> WorkflowRunStatus.DONE;
            case "action_required", "cancelled", "failure",
                    "neutral", "skipped", "stale", "timed_out" ->
                    WorkflowRunStatus.FAILED;
            case "in_progress" -> WorkflowRunStatus.IN_PROGRESS;
            case "queued", "requested", "waiting", "pending" ->
                    WorkflowRunStatus.PENDING;
            default ->
                    throw new IllegalStateException("Unexpected value: " + status);
        };
    }

    boolean isActive() {
        return List.of("in_progress",
                        "queued",
                        "requested",
                        "waiting",
                        "pending")
                .contains(this.status());
    }
}
