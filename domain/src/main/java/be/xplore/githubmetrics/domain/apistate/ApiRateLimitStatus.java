package be.xplore.githubmetrics.domain.apistate;

import java.util.Arrays;
import java.util.function.IntUnaryOperator;

public enum ApiRateLimitStatus {
    CRITICAL, WARNING, CONCERNING, GOOD, OK;

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
        return this.newStatus(ordinal -> ordinal + 1);
    }

    public ApiRateLimitStatus getNextWorst() {
        return this.newStatus(ordinal -> ordinal - 1);
    }

    private ApiRateLimitStatus newStatus(IntUnaryOperator function) {
        var newStatus = Math.clamp(
                function.applyAsInt(this.ordinal()), min().ordinal(), max().ordinal()
        );
        return values()[newStatus];
    }

    public ApiRateLimitStatus max() {
        return Arrays.stream(values()).max(ApiRateLimitStatus::compareTo).orElseThrow();
    }

    public ApiRateLimitStatus min() {
        return Arrays.stream(values()).min(ApiRateLimitStatus::compareTo).orElseThrow();
    }
}

