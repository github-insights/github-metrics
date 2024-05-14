package be.xplore.githubmetrics.domain.workflowrun;

import be.xplore.githubmetrics.domain.repository.Repository;

public class WorkflowRun {
    private final long id;
    private final String name;
    private final WorkflowRunStatus status;
    private final Repository repository;

    private int buildTime;

    public WorkflowRun(
            long id,
            String name,
            WorkflowRunStatus status,
            Repository repository
    ) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.repository = repository;
    }

    public WorkflowRun(
            long id,
            String name,
            WorkflowRunStatus status,
            Repository repository,
            int buildTime
    ) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.repository = repository;
        this.buildTime = buildTime;
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

    public int getBuildTime() {
        return buildTime;
    }

    public void setBuildTime(int buildTime) {
        this.buildTime = buildTime;
    }
}