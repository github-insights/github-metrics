package be.xplore.githubmetrics.domain.apistate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum ApiRateLimitStatus {
    CRITICAL, WARNING, CONCERNING, GOOD, OK;

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiRateLimitStatus.class);

    public boolean isCritical() {
        return this.equals(CRITICAL);
    }

    public boolean isWarningOrWorse() {
        return isCritical() || this.equals(WARNING);
    }

    public boolean isConcerningOrWorse() {
        return isWarningOrWorse() || this.equals(CONCERNING);
    }

    public boolean isGoodOrWorse() {
        return isConcerningOrWorse() || this.equals(GOOD);
    }

    public ApiRateLimitStatus getNextBest() {
        return switch (this) {
            case CRITICAL -> WARNING;
            case WARNING -> CONCERNING;
            case CONCERNING -> GOOD;
            case GOOD, OK -> OK;
        };
    }

    public boolean isBetterThen(ApiRateLimitStatus other) {
        var better = this.ordinal() > other.ordinal();
        LOGGER.debug(
                "The status {} is better then the status {}: {}",
                this, other, better
        );
        return better;
    }
}

