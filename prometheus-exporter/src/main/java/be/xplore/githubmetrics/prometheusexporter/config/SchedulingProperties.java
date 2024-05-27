package be.xplore.githubmetrics.prometheusexporter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.scheduling.exporters")
public record SchedulingProperties(
        String workflowRuns,
        String activeWorkflowRuns,
        String workflowRunBuildTimes,
        String jobs,
        String pullRequests,
        String selfHostedRunners,
        String repositoryCount,
        String apiRateLimitState
) {
}
