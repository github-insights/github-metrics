package be.xplore.githubmetrics.githubadapter.config.ratelimiting;

import be.xplore.githubmetrics.domain.apistate.ApiRateLimitState;
import be.xplore.githubmetrics.domain.apistate.ApiRateLimitStateQueryPort;
import org.springframework.stereotype.Service;

@Service
public class ApiRateLimitStateAdapter implements ApiRateLimitStateQueryPort {
    private final ApiRateLimitState rateLimitState;

    public ApiRateLimitStateAdapter(ApiRateLimitState rateLimitState) {
        this.rateLimitState = rateLimitState;
    }

    @Override
    public ApiRateLimitState getCurrentApiRateLimitState() {
        return this.rateLimitState;
    }
}
