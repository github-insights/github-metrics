package be.xplore.githubmetrics.domain.apistate;

public interface ApiRateLimitStateQueryPort {
    ApiRateLimitState getCurrentApiRateLimitState();
}
