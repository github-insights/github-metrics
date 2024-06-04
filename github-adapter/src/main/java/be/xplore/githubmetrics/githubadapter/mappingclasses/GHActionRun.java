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
        String conclusion,
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
        return switch (this.conclusion) {
            case "failure", "cancelled", "startup_failure" ->
                    WorkflowRunStatus.FAILED;
            case "success" -> WorkflowRunStatus.DONE;
            case "neutral", "in_progress" -> this.statusFromStatus();
            case null -> this.statusFromStatus();
            default ->
                    throw new IllegalStateException("Unexpected value: \"" + conclusion + "\"");
        };
    }

    WorkflowRunStatus statusFromStatus() {
        return switch (this.status) {
            case "completed", "success" -> WorkflowRunStatus.DONE;
            case "action_required" -> WorkflowRunStatus.ACTION_REQUIRED;
            case "cancelled", "failure", "neutral",
                 "skipped", "stale", "timed_out" -> WorkflowRunStatus.FAILED;
            case "in_progress" -> WorkflowRunStatus.IN_PROGRESS;
            case "queued", "requested", "waiting", "pending" ->
                    WorkflowRunStatus.PENDING;
            default ->
                    throw new IllegalStateException("Unexpected value: \"" + status + "\"");
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
