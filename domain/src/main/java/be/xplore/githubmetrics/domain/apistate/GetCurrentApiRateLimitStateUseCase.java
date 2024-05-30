package be.xplore.githubmetrics.domain.apistate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GetCurrentApiRateLimitStateUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetCurrentApiRateLimitStateUseCase.class);
    private final ApiRateLimitStateQueryPort apiRateLimitStateQueryPort;

    public GetCurrentApiRateLimitStateUseCase(
            ApiRateLimitStateQueryPort apiRateLimitStateQueryPort
    ) {
        this.apiRateLimitStateQueryPort = apiRateLimitStateQueryPort;
    }

    public ApiRateLimitState getCurrentApiRateLimitState() {
        LOGGER.debug("Exporting current Api rate limit state");
        return this.apiRateLimitStateQueryPort.getCurrentApiRateLimitState();
    }
}
