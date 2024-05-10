package be.xplore.githubmetrics.domain.job;

public class Job {
    private final long id;
    private final long runId;
    private final JobStatus status;
    private final JobConclusion conclusion;

    public Job(long id, long runId, JobStatus status, JobConclusion conclusion) {
        this.id = id;
        this.runId = runId;
        this.status = status;
        this.conclusion = conclusion;
    }

    public JobStatus getStatus() {
        return status;
    }

    public JobConclusion getConclusion() {
        return conclusion;
    }

    @Override
    public String toString() {
        return "Job{" +
                "id=" + id +
                ", runId=" + runId +
                ", status=" + status +
                ", conclusion=" + conclusion +
                '}';
    }
}
