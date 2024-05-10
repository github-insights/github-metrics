package be.xplore.githubmetrics.githubadapter.mappingclasses;

import be.xplore.githubmetrics.domain.job.Job;
import be.xplore.githubmetrics.domain.job.JobConclusion;
import be.xplore.githubmetrics.domain.job.JobStatus;

public record GHJob(
        long id,
        long run_id,
        String status,
        String conclusion
) {

    Job getJob() {

        return new Job(
                this.id,
                this.run_id,
                convertStatus(),
                convertConclusion()

        );
    }

    private JobConclusion convertConclusion() {
        return switch (this.conclusion) {
            case "success" -> JobConclusion.SUCCESS;
            case "failure",
                    "cancelled",
                    "timed_out",
                    "action_required" -> JobConclusion.FAILURE;
            case "neutral", "skipped" -> JobConclusion.NEUTRAL;
            case null -> JobConclusion.NEUTRAL;
            default -> throw new IllegalStateException(
                    "Unexpected GH Job Conclusion Value: " + this.conclusion
            );
        };
    }

    private JobStatus convertStatus() {
        return switch (this.status) {
            case "requested", "queued", "pending" -> JobStatus.PENDING;
            case "in_progress" -> JobStatus.IN_PROGRESS;
            case "completed" -> JobStatus.DONE;
            default -> throw new IllegalStateException(
                    "Unexpected GH Job Status value: " + this.status
            );
        };
    }
}
