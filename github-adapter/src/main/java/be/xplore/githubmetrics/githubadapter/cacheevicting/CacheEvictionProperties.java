package be.xplore.githubmetrics.githubadapter.cacheevicting;

import be.xplore.githubmetrics.domain.apistate.ApiRateLimitStatus;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.scheduling.cacheeviction")
public record CacheEvictionProperties(
        EvictionState workflowRuns,
        EvictionState workflowRunBuildTimes,
        EvictionState jobs,
        EvictionState pullRequests,
        EvictionState selfHostedRunners,
        EvictionState repositoryCount
) {

    public record EvictionState(
            String schedule,
            ApiRateLimitStatus status
    ) {
    }

}
