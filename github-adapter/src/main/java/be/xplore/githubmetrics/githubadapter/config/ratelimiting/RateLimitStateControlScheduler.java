package be.xplore.githubmetrics.githubadapter.config.ratelimiting;

import be.xplore.githubmetrics.domain.apistate.ApiRateLimitState;
import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

@Service
public class RateLimitStateControlScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitStateControlScheduler.class);
    private final ApiRateLimitState rateLimitState;
    private long lastUsedRequests;

    public RateLimitStateControlScheduler(
            GithubProperties githubProperties,
            @Qualifier("githubAdapterTaskScheduler") TaskScheduler taskScheduler,
            ApiRateLimitState rateLimitState
    ) {
        this.rateLimitState = rateLimitState;
        this.lastUsedRequests = rateLimitState.getUsed();

        String controlSchedule = githubProperties.ratelimiting().stateControlCheckSchedule();

        LOGGER.info("Rate-limit state control scheduler is on schedule {}.", controlSchedule);

        taskScheduler.schedule(
                this::controlApiRateLimitState,
                triggerContext -> new CronTrigger(controlSchedule)
                        .nextExecution(triggerContext)
        );
    }

    private void controlApiRateLimitState() {
        LOGGER.info(
                "Rate limit control scheduler ran with previous used {} and current used {}.",
                this.lastUsedRequests,
                this.rateLimitState.getUsed()
        );
        if (this.lastUsedRequests == this.rateLimitState.getUsed()) {
            var previousStatus = this.rateLimitState.getStatus();
            this.rateLimitState.lowerStatusByOne();
            LOGGER.info(
                    "Since previous used and current used are equal status will be lowered from {} to {}.",
                    previousStatus, this.rateLimitState.getStatus()
            );
        }
        this.lastUsedRequests = this.rateLimitState.getUsed();
    }
}
