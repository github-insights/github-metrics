package be.xplore.githubmetrics.githubadapter.mappingclasses;

import be.xplore.githubmetrics.domain.domain.Job;

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

    private Job.JobConclusion convertConclusion() {
        return switch (this.conclusion) {
            case "success" -> Job.JobConclusion.SUCCESS;
            case "failure",
                    "cancelled",
                    "timed_out",
                    "action_required" -> Job.JobConclusion.FAILURE;
            case "neutral", "skipped" -> Job.JobConclusion.NEUTRAL;
            case null -> Job.JobConclusion.NEUTRAL;
            default -> throw new IllegalStateException(
                    "Unexpected GH Job Conclusion Value: " + this.conclusion
            );
        };
    }

    private Job.JobStatus convertStatus() {
        return switch (this.status) {
            case "requested", "queued", "pending" -> Job.JobStatus.PENDING;
            case "in_progress" -> Job.JobStatus.IN_PROGRESS;
            case "completed" -> Job.JobStatus.DONE;
            default -> throw new IllegalStateException(
                    "Unexpected GH Job Status value: " + this.status
            );
        };
    }
}
