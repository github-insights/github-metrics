package be.xplore.githubmetrics.domain.apistate;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

public class ApiRateLimitState {
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
    private double actualRequestsPerMilli;
    private double idealRequestsPerMilli;

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
            this.actualRequestsPerMilli = actualRequestsPerSecond;
            this.idealRequestsPerMilli = idealRequestsPerSecond;

            var newStatus = this.getNewAbsoluteStatus();

            if (newStatus.isBetterThen(currentStatus)) {
                this.setActualStatus(currentStatus.getNextBest());
            } else {
                this.setActualStatus(newStatus);
            }
        });
    }

    public ApiRateLimitStatus getNewAbsoluteStatus() {
        ApiRateLimitStatus newStatus;
        if (this.idealRequestsPerMilli * this.goodLimit >= this.actualRequestsPerMilli) {
            newStatus = ApiRateLimitStatus.OK;
        } else if (this.idealRequestsPerMilli * this.concerningLimit >= this.actualRequestsPerMilli) {
            newStatus = ApiRateLimitStatus.GOOD;
        } else if (this.idealRequestsPerMilli * this.warningLimit >= this.actualRequestsPerMilli) {
            newStatus = ApiRateLimitStatus.CONCERNING;
        } else if (this.idealRequestsPerMilli * this.criticalLimit >= this.actualRequestsPerMilli) {
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

    public double getActualRequestsPerMilli() {
        return actualRequestsPerMilli;
    }

    public double getIdealRequestsPerMilli() {
        return idealRequestsPerMilli;
    }

    public Optional<ApiRateLimitStatus> getStatus() {
        return status;
    }
}