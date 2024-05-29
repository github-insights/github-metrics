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
    private long lastRemainingRequests;

    public RateLimitStateControlScheduler(
            GithubProperties githubProperties,
            @Qualifier("githubAdapterTaskScheduler") TaskScheduler taskScheduler,
            ApiRateLimitState rateLimitState
    ) {
        this.rateLimitState = rateLimitState;
        this.lastRemainingRequests = rateLimitState.getRemaining();

        String controlSchedule = githubProperties.ratelimiting().stateControlCheckSchedule();

        LOGGER.error("Rate-limit state control scheduler is on schedule {}.", controlSchedule);

        taskScheduler.schedule(
                this::controlApiRateLimitState,
                triggerContext -> new CronTrigger(controlSchedule)
                        .nextExecution(triggerContext)
        );
    }

    private void controlApiRateLimitState() {
        if (this.rateLimitState.getUsed() == this.lastRemainingRequests) {
            this.rateLimitState.lowerStatusByOne();
        }
        this.lastRemainingRequests = rateLimitState.getUsed();
    }
}
