package be.xplore.githubmetrics.githubadapter.config.ratelimiting;

public record RateLimitHeaders(
        long limit,
        long remaining,
        long reset,
        long used
) {
}
