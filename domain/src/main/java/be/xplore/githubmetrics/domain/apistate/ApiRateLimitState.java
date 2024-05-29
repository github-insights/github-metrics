package be.xplore.githubmetrics.domain.apistate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

public class ApiRateLimitState {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiRateLimitState.class);

    private final double rateLimitBuffer;
    private final double criticalLimit;
    private final double warningLimit;
    private final double concerningLimit;
    private final double goodLimit;
    private long limit;
    private long remaining;
    private long reset;
    private long used;
    private Optional<ApiRateLimitStatus> status = Optional.of(ApiRateLimitStatus.OK);
    private double actualRequestsPerSecond;
    private double idealRequestsPerSecond;

    public ApiRateLimitState(
            double rateLimitBuffer,
            double criticalLimit,
            double warningLimit,
            double concerningLimit,
            double goodLimit
    ) {
        this.rateLimitBuffer = rateLimitBuffer;
        this.criticalLimit = criticalLimit;
        this.warningLimit = warningLimit;
        this.concerningLimit = concerningLimit;
        this.goodLimit = goodLimit;
    }

    public void recalculateStatus(
            double actualRequestsPerSecond,
            double idealRequestsPerSecond
    ) {
        this.status.ifPresent(currentStatus -> {
            this.actualRequestsPerSecond = actualRequestsPerSecond;
            this.idealRequestsPerSecond = idealRequestsPerSecond;

            var newStatus = this.getNewAbsoluteStatus();
            LOGGER.trace(
                    "Current status is {} and possible new Status is {}.",
                    currentStatus, newStatus
            );
            if (newStatus.isBetterThen(currentStatus)) {
                this.lowerStatusByOne();
            } else {
                this.setActualStatus(newStatus);
            }

            LOGGER.debug("New Status is {}.", this.getStatus());
        });
    }

    public void lowerStatusByOne() {
        this.status.ifPresent(currentStatus ->
                this.setActualStatus(currentStatus.getNextBest())
        );
    }

    public ApiRateLimitStatus getNewAbsoluteStatus() {
        ApiRateLimitStatus newStatus;
        if (this.idealRequestsPerSecond * this.goodLimit >= this.actualRequestsPerSecond) {
            newStatus = ApiRateLimitStatus.OK;
        } else if (this.idealRequestsPerSecond * this.concerningLimit >= this.actualRequestsPerSecond) {
            newStatus = ApiRateLimitStatus.GOOD;
        } else if (this.idealRequestsPerSecond * this.warningLimit >= this.actualRequestsPerSecond) {
            newStatus = ApiRateLimitStatus.CONCERNING;
        } else if (this.idealRequestsPerSecond * this.criticalLimit >= this.actualRequestsPerSecond) {
            newStatus = ApiRateLimitStatus.WARNING;
        } else {
            newStatus = ApiRateLimitStatus.CRITICAL;
        }
        return newStatus;
    }

    public boolean limitHasBeenHit() {
        return this.getRemaining() == 0;
    }

    public Duration getDurationToReset() {
        var now = ZonedDateTime.now();
        return Duration.between(now, this.getReset(now.getOffset()));
    }

    private void setActualStatus(ApiRateLimitStatus status) {
        this.status = Optional.of(status);
    }

    public boolean shouldDataWait(ApiRateLimitStatus limit) {
        return this.status.map(currentStatus ->
                switch (limit) {
                    case null -> false;
                    case CRITICAL -> currentStatus.isCritical();
                    case WARNING -> currentStatus.isWarningOrWorse();
                    case CONCERNING -> currentStatus.isConcerningOrWorse();
                    case GOOD -> currentStatus.isGoodOrWorse();
                    case OK -> currentStatus.equals(ApiRateLimitStatus.OK);
                }
        ).orElse(true);
    }

    public Runnable stopRequestsAndGetResetFunction() {
        this.status = Optional.empty();
        return this::resetStatusToCritical;
    }

    private void resetStatusToCritical() {
        this.status = Optional.of(ApiRateLimitStatus.CRITICAL);
    }

    public long getLimit() {
        return (long) (this.limit * this.rateLimitBuffer);
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public long getRemaining() {
        return remaining;
    }

    public void setRemaining(long remaining) {
        this.remaining = remaining;
    }

    public ZonedDateTime getReset(ZoneId id) {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(this.reset), id);
    }

    public long getReset() {
        return reset;
    }

    public void setReset(long reset) {
        this.reset = reset;
    }

    public long getUsed() {
        return used;
    }

    public void setUsed(long used) {
        this.used = used;
    }

    public double getActualRequestsPerSecond() {
        return actualRequestsPerSecond;
    }

    public double getIdealRequestsPerSecond() {
        return idealRequestsPerSecond;
    }

    public Optional<ApiRateLimitStatus> getStatus() {
        return status;
    }
}