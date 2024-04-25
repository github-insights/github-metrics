package be.xplore.githubmetrics.domain.domain;

public class WorkflowRun {
    private final long id;
    private final String name;
    private final RunStatus status;

    public WorkflowRun(long id, String name, RunStatus status) {
        this.id = id;
        this.name = name;
        this.status = status;
    }

    @Override
    public String toString() {
        return "WorkflowRun{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                '}';
    }

    public String getName() {
        return name;
    }

    public RunStatus getStatus() {
        return status;
    }

    public long getId() {
        return id;
    }

    public enum RunStatus {
        DONE, PENDING, FAILED
    }
}
