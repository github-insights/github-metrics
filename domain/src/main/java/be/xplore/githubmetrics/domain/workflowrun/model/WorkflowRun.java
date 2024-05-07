package be.xplore.githubmetrics.domain.workflowrun.model;

import be.xplore.githubmetrics.domain.repository.Repository;

public class WorkflowRun {
    private final long id;
    private final String name;
    private final WorkflowRunStatus status;
    private final Repository repository;

    public WorkflowRun(long id, String name, WorkflowRunStatus status, Repository repository) {
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

    public WorkflowRunStatus getStatus() {
        return status;
    }

    public long getId() {
        return id;
    }

    public Repository getRepository() {
        return repository;
    }
}