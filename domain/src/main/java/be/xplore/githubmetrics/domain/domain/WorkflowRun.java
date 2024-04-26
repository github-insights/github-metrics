package be.xplore.githubmetrics.domain.domain;

public class WorkflowRun {
    private final long id;
    private final String name;
    private final RunStatus status;
    private final Repository repository;

    public WorkflowRun(long id, String name, RunStatus status, Repository repository) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.repository = repository;
    }

    @Override
    public String toString() {
        return "WorkflowRun{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", repository='" + repository + '\'' +
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

    public Repository getRepository() {
        return repository;
    }

    public enum RunStatus {
        DONE, PENDING, FAILED
    }
}
