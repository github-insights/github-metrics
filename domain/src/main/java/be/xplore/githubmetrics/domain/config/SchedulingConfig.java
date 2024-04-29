package be.xplore.githubmetrics.domain.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.scheduling")
public record SchedulingConfig(
        String workflowRunsInterval,
        String jobsInterval
) {
}
