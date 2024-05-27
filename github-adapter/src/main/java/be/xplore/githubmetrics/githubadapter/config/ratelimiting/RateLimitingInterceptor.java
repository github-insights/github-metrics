package be.xplore.githubmetrics.githubadapter.config.ratelimiting;

import be.xplore.githubmetrics.domain.apistate.ApiRateLimitState;
import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Component
public class RateLimitingInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitingInterceptor.class);
    private final RateLimitResetAwaitScheduler resetAwaitScheduler;
    private final ApiRateLimitState rateLimitState;
    private final long secondsBetweenStateRecalculations;
    private final Duration requestCountingDuration;
    private ZonedDateTime startRequestCounting = now();
    private Optional<Long> usedRequestsAtCountingStart = Optional.empty();
    private Optional<Long> remainingRequestsAtCountingStart = Optional.empty();

    public RateLimitingInterceptor(
            GithubProperties githubProperties,
            RateLimitResetAwaitScheduler resetAwaitScheduler,
            ApiRateLimitState rateLimitState
    ) {
        this.secondsBetweenStateRecalculations = githubProperties.ratelimiting()
                .secondsBetweenStateRecalculations();
        this.requestCountingDuration = getCountingDuration();
        this.resetAwaitScheduler = resetAwaitScheduler;
        this.rateLimitState = rateLimitState;
    }

    /**
     * <p>
     * Intercepts the request and gets the rate-limit headers from the response.
     * With these then updates the ApiRateLimitState accordingly. To gauge how
     * many requests are being made the interceptor waits a specific period
     * which it then uses to make its judgements.
     * </p>
     *
     * <p>
     * During the update progress there are a few special cases:
     * - There are no requests left and all requesting needs to be stopped
     * - The limit has been reset which needs to be handled correctly.
     * </p>
     *
     * @param request
     * @param body
     * @param execution
     * @return
     * @throws IOException
     */
    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, byte[] body, ClientHttpRequestExecution execution
    ) throws IOException {
        var response = execution.execute(request, body);
        this.updateRateLimitHeaders(response.getHeaders());

        this.handleHittingRateLimit();

        if (remainingRequestsHaveDecreased()) {
            this.setStartingStateIfEmpty();
            this.updateRateLimitState();
        }

        return response;
    }

    private void handleHittingRateLimit() {
        if (this.rateLimitState.limitHasBeenHit()) {
            var resetFunction = this.rateLimitState.stopRequestsAndGetResetFunction();
            this.resetAwaitScheduler.createStopAllRequestsTask(
                    resetFunction, this.rateLimitState.getDurationToReset()
            );
        }
    }

    private void setStartingStateIfEmpty() {
        if (this.usedRequestsAtCountingStart.isEmpty()) {
            this.usedRequestsAtCountingStart = Optional.of(this.rateLimitState.getUsed());
        }
        if (this.remainingRequestsAtCountingStart.isEmpty()) {
            this.remainingRequestsAtCountingStart = Optional.of(this.rateLimitState.getRemaining());
        }
    }

    /**
     * A simple way to find out if a rate-limit reset has happened is by looking
     * at the remaining requests. If they have increased then that means a reset
     * has happened.
     *
     * @return If a reset has happened
     */
    private boolean remainingRequestsHaveDecreased() {
        return this.remainingRequestsAtCountingStart.map(previousRemainingRequests -> {
            var resetHasNotHappened = previousRemainingRequests > this.rateLimitState.getRemaining();
            if (!resetHasNotHappened) {
                LOGGER.error(
                        "Rate limit reset seems to have happend. Remaining jumped form {} to {}",
                        previousRemainingRequests, this.rateLimitState.getRemaining()
                );
                this.resetInterceptor();
            }
            return resetHasNotHappened;
        }).orElse(true);
    }

    /**
     * If the waiting period is over then this function will first call the
     * recalculateStatus on ApiRateLimitState with the information gathered in
     * the period. After which the interceptor will reset itself for the next
     * period.
     */
    private void updateRateLimitState() {
        if (startRequestCounting.plus(requestCountingDuration).isBefore(now())) {
            this.logRateLimitStateBefore();

            double numRequestsInDuration = this.rateLimitState.getUsed() - (double) usedRequestsAtCountingStart.get();
            double secondsBetweenCountingStartAndNow = ChronoUnit.SECONDS.between(this.startRequestCounting, now());
            double actualRequestsPerSecond = numRequestsInDuration > 0
                    ? numRequestsInDuration / secondsBetweenCountingStartAndNow
                    : 0;

            this.rateLimitState.recalculateStatus(
                    actualRequestsPerSecond,
                    getIdealProportions()
            );

            this.resetInterceptor();

            this.logRateLimitStateAfter(numRequestsInDuration);
        }
    }

    /**
     * Calculates the ideal number of requests from the start of the waiting
     * period to the time specified by the rate limiting headers. Will also use
     * the remaining requests for this calculation.
     *
     * @return Ideal number of requests in remaining time
     */
    private double getIdealProportions() {
        double remaining = remainingRequestsAtCountingStart.get();
        var resetTime = this.rateLimitState.getReset(
                this.startRequestCounting.getOffset()
        );
        double timeRange = ChronoUnit.SECONDS.between(
                startRequestCounting, resetTime
        );
        return remaining / timeRange;
    }

    private void resetInterceptor() {
        startRequestCounting = now();
        usedRequestsAtCountingStart = Optional.empty();
        remainingRequestsAtCountingStart = Optional.empty();
    }

    private void updateRateLimitHeaders(HttpHeaders headers) {
        getLongFromHeaders(headers, "x-ratelimit-limit").ifPresent(this.rateLimitState::setLimit);
        getLongFromHeaders(headers, "x-ratelimit-remaining").ifPresent(this.rateLimitState::setRemaining);
        getLongFromHeaders(headers, "x-ratelimit-reset").ifPresent(this.rateLimitState::setReset);
        getLongFromHeaders(headers, "x-ratelimit-used").ifPresent(this.rateLimitState::setUsed);
    }

    private Optional<Long> getLongFromHeaders(HttpHeaders headers, String headerName) {
        return Optional.ofNullable(headers.get(headerName)).map(headerList -> {
            if (headerList.size() > 1) {
                LOGGER.debug("Ratelimiting header {} contains more then 1 value.", headerName);
            }
            return Long.parseLong(headerList.getFirst());
        }).or(() -> {
            LOGGER.warn("Ratelimiting header {} not present in headers!", headerName);
            return Optional.empty();
        });
    }

    private Duration getCountingDuration() {
        return Duration.ofSeconds(this.secondsBetweenStateRecalculations);
    }

    private ZonedDateTime now() {
        return ZonedDateTime.now();
    }

    private void logRateLimitStateBefore() {
        LOGGER.info("State before is {}", this.rateLimitState.getStatus());
        LOGGER.trace(
                "Rate limit used requests was {} and now is {}",
                usedRequestsAtCountingStart.get(), this.rateLimitState.getUsed()
        );
    }

    private void logRateLimitStateAfter(double numRequestsInDuration) {
        LOGGER.debug("Ratelimiting info:");
        LOGGER.debug("{} Requests in period {}", numRequestsInDuration, this.getCountingDuration());
        LOGGER.debug(
                "Actual {} req/s / Ideal {} req/s",
                this.rateLimitState.getActualRequestsPerMilli(), this.rateLimitState.getIdealRequestsPerMilli()
        );
        LOGGER.trace(
                "Limit {} Remaining {} Used {}",
                this.rateLimitState.getLimit(), this.rateLimitState.getRemaining(),
                this.rateLimitState.getUsed()
        );
        LOGGER.trace(
                "Remaining minutes until limit reset {}",
                ChronoUnit.MINUTES.between(now(), this.rateLimitState.getReset(now().getOffset()))
        );
        LOGGER.info("State after is {}", this.rateLimitState.getStatus());
    }
}