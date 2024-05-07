package be.xplore.githubmetrics.githubadapter.mappingclasses;

import be.xplore.githubmetrics.domain.job.model.Job;

import java.util.List;

public record GHWorkflowRunJobs(
        int total_count,
        List<GHJob> jobs
) {
    public List<Job> getJobs() {
        return this.jobs.stream().map(
                GHJob::getJob
        ).toList();
    }
}
