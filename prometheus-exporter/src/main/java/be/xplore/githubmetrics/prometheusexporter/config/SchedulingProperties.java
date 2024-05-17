package be.xplore.githubmetrics.prometheusexporter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.scheduling")
public record SchedulingProperties(
        String workflowRunsInterval,
        String workflowRunBuildTimesInterval,
        String jobsInterval,
        String pullRequestsInterval,
        String selfHostedRunnersInterval,
        String repositoryCountInterval
) {
}
