package be.xplore.githubmetrics.prometheusexporter.job;

import be.xplore.githubmetrics.domain.job.JobConclusion;
import be.xplore.githubmetrics.domain.job.JobStatus;

public record JobLabel(
        JobStatus status,
        JobConclusion conclusion
) {
}
