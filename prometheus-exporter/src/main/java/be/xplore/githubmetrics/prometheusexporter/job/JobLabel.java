package be.xplore.githubmetrics.prometheusexporter.job;

import be.xplore.githubmetrics.domain.job.model.JobConclusion;
import be.xplore.githubmetrics.domain.job.model.JobStatus;

public record JobLabel(
        JobStatus status,
        JobConclusion conclusion
) {
}
