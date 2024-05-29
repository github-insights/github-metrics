package be.xplore.githubmetrics.githubadapter.config.ratelimiting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

@Service
public class RateLimitResetAwaitScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitResetAwaitScheduler.class);
    private final TaskScheduler taskScheduler;
    private Optional<ScheduledFuture<?>> stopEvicting = Optional.empty();

    public RateLimitResetAwaitScheduler(
            @Qualifier("githubAdapterTaskScheduler") TaskScheduler taskScheduler
    ) {
        this.taskScheduler = taskScheduler;
    }

    public void createStopAllRequestsTask(
            Runnable reset, Duration restartTime
    ) {
        this.stopEvicting = Optional.of(this.taskScheduler.scheduleWithFixedDelay(
                () -> {
                    reset.run();
                    this.stopEvicting.get().cancel(false);
                    this.stopEvicting = Optional.empty();
                    LOGGER.trace("Finished reset await task. Remove the future.");
                },
                restartTime
        ));
        LOGGER.debug(
                "Reset await task has been created, will run reset in {}s.",
                restartTime.toSeconds()
        );
    }
}