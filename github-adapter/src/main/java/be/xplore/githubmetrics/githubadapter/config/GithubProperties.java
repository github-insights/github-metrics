package be.xplore.githubmetrics.githubadapter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.github")
public record GithubProperties(
        String url,
        String org,
        Application application,
        RateLimiting ratelimiting
) {
    public record Application(
            String id,
            String installId,
            String pem
    ) {
    }

    public record RateLimiting(
            long secondsBetweenStateRecalculations,
            double rateLimitBuffer,
            double criticalLimit,
            double warningLimit,
            double concerningLimit,
            double goodLimit
    ) {
    }
}