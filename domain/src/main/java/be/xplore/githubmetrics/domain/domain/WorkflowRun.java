package be.xplore.githubmetrics.domain.domain;

import com.fasterxml.jackson.databind.JsonNode;

public class WorkflowRun {
    private final int id;
    private final String name;
    private RunStatus status;

    public WorkflowRun(JsonNode json) {
        this.id = json.get("id").asInt();
        this.name = json.get("name").toString();
        this.status = switch (json.get("status").asText()) {
            case "completed", "success" -> RunStatus.DONE;
            case "action_required", "cancelled", "failure",
                 "neutral", "skipped", "stale", "timed_out" -> RunStatus.FAILED;
            case "in_progress", "queued", "requested",
                 "waiting", "pending" -> RunStatus.PENDING;
            default -> throw new IllegalStateException("Unexpected value: " + status);
        };
    }

    @Override
    public String toString() {
        return "WorkflowRun{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                '}';
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public RunStatus getStatus() {
        return status;
    }

    public enum RunStatus {
        DONE, PENDING, FAILED
    }
}
