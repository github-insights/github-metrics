package be.xplore.githubmetrics.githubadapter.config.ratelimiting;

import be.xplore.githubmetrics.domain.apistate.ApiRateLimitState;
import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitingConfig {

    @Bean
    public ApiRateLimitState apiRateLimitState(GithubProperties properties) {
        return new ApiRateLimitState(
                properties.ratelimiting().rateLimitBuffer(),
                properties.ratelimiting().criticalLimit(),
                properties.ratelimiting().warningLimit(),
                properties.ratelimiting().concerningLimit(),
                properties.ratelimiting().goodLimit()
        );
    }
}