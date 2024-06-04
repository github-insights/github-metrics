package be.xplore.githubmetrics.githubadapter.config.ratelimiting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
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
            Runnable reset, Instant restartTime
    ) {
        if (this.stopEvicting.isEmpty()) {
            this.stopEvicting = Optional.of(this.taskScheduler.schedule(
                    () -> {
                        reset.run();
                        this.stopEvicting.get().cancel(false);
                        this.stopEvicting = Optional.empty();
                        LOGGER.info("Finished reset await task. Remove the future.");
                    },
                    restartTime
            ));
            LOGGER.warn(
                    "Reset await task has been created, will run reset at {}.",
                    restartTime
            );
        }
    }
}