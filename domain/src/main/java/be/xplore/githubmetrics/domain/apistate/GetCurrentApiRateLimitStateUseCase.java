package be.xplore.githubmetrics.domain.apistate;

import org.springframework.stereotype.Component;

@Component
public class GetCurrentApiRateLimitStateUseCase {

    private final ApiRateLimitStateQueryPort apiRateLimitStateQueryPort;

    public GetCurrentApiRateLimitStateUseCase(
            ApiRateLimitStateQueryPort apiRateLimitStateQueryPort
    ) {
        this.apiRateLimitStateQueryPort = apiRateLimitStateQueryPort;
    }

    public ApiRateLimitState getCurrentApiRateLimitState() {
        return this.apiRateLimitStateQueryPort.getCurrentApiRateLimitState();
    }
}
