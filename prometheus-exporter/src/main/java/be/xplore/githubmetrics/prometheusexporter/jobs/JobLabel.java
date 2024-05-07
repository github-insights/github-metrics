package be.xplore.githubmetrics.prometheusexporter.jobs;

import be.xplore.githubmetrics.domain.domain.Job;

public record JobLabel(
        Job.JobStatus status,
        Job.JobConclusion conclusion
) {
}
