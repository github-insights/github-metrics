package be.xplore.githubmetrics.githubadapter.ratelimiting;

import be.xplore.githubmetrics.domain.apistate.ApiRateLimitState;
import be.xplore.githubmetrics.githubadapter.TestUtility;
import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import be.xplore.githubmetrics.githubadapter.config.ratelimiting.RateLimitResetAwaitScheduler;
import be.xplore.githubmetrics.githubadapter.config.ratelimiting.RateLimitingInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RateLimitingInterceptorTest {

    private final ApiRateLimitState mockRateLimitState = mock(ApiRateLimitState.class);
    private final RateLimitResetAwaitScheduler awaitScheduler = mock(RateLimitResetAwaitScheduler.class);
    private final GithubProperties mockProperties = mock(GithubProperties.class);
    private RateLimitingInterceptor rateLimitingInterceptor;
    private ClientHttpResponse mockResponse;

    @BeforeEach
    void setUp() {
        var mockRateLimiting = mock(GithubProperties.RateLimiting.class);
        when(mockProperties.ratelimiting()).thenReturn(mockRateLimiting);
        when(mockRateLimiting.secondsBetweenStateRecalculations()).thenReturn(1L);
        when(this.mockRateLimitState.getReset(any(ZoneId.class))).thenReturn(ZonedDateTime.now().plus(Duration.ofMinutes(10)));

        this.rateLimitingInterceptor = new RateLimitingInterceptor(
                mockProperties, awaitScheduler, mockRateLimitState
        );
    }

    @Test
    void interceptorShouldParseRateLimitingHeadersCorrectly() throws IOException {
        when(this.mockRateLimitState.limitHasBeenHit()).thenReturn(false);

        long number = 34_235L;
        this.createHeadersAndCallIntercept(this.rateLimitingInterceptor, number);

        verify(this.mockRateLimitState).setLimit(number);
        verify(this.mockRateLimitState).setRemaining(number);
        verify(this.mockRateLimitState).setReset(number);
        verify(this.mockRateLimitState).setUsed(number);
    }

    @Test
    void hittingRateLimitShouldCreateStopTask() throws IOException {
        var interceptorWithRealState = new RateLimitingInterceptor(
                this.mockProperties, awaitScheduler,
                TestUtility.getApiRateLimitState()
        );
        this.createHeadersAndCallIntercept(
                interceptorWithRealState,
                5000, 0, getInOneMinuteEpochSeconds(), 5000
        );

        verify(awaitScheduler).createStopAllRequestsTask(
                any(Runnable.class), any(Duration.class)
        );
    }

    @Test
    void resettingRemainingRequestsShouldNotCauseSecondStatusRecalculation() throws IOException, InterruptedException {
        this.rateLimitingInterceptor = new RateLimitingInterceptor(
                mockProperties, awaitScheduler, mockRateLimitState
        );

        when(mockRateLimitState.getRemaining())
                .thenReturn(2L)
                .thenReturn(1L)
                .thenReturn(5000L);

        Thread.sleep(1000L);

        this.createHeadersAndCallIntercept(
                this.rateLimitingInterceptor,
                5000, 2, getInOneMinuteEpochSeconds(), 4998
        );
        this.createHeadersAndCallIntercept(
                this.rateLimitingInterceptor,
                5000, 1, getInOneMinuteEpochSeconds(), 4999
        );

        Thread.sleep(1000L);

        this.createHeadersAndCallIntercept(
                this.rateLimitingInterceptor,
                5000, 5000, getInOneMinuteEpochSeconds(), 0
        );

        verify(this.mockRateLimitState).recalculateStatus(anyDouble(), anyDouble());
    }

    private long getInOneMinuteEpochSeconds() {
        return Instant.now().plus(Duration.ofMinutes(1)).getEpochSecond();
    }

    private void createHeadersAndCallIntercept(
            RateLimitingInterceptor rateLimitingInterceptor, long number
    ) throws IOException {
        this.createHeadersAndCallIntercept(
                rateLimitingInterceptor,
                number, number, number, number
        );
    }

    @SuppressWarnings("checkstyle:parameternumber")
    private void createHeadersAndCallIntercept(
            RateLimitingInterceptor rateLimitingInterceptor,
            long limit, long remaining, long reset, long used
    ) throws IOException {

        var mockExecution = getExecution(limit, remaining, reset, used);
        rateLimitingInterceptor.intercept(
                mock(HttpRequest.class), new byte[1], mockExecution
        );
        this.mockResponse.close();
    }

    @SuppressWarnings("checkstyle:parameternumber")
    private ClientHttpRequestExecution getExecution(
            long limit, long remaining, long reset, long used
    ) throws IOException {
        var mockExecution = mock(ClientHttpRequestExecution.class);
        mockResponse = mock(ClientHttpResponse.class);

        var httpHeaders = getGithubRatelimitingHeaders(
                limit, remaining, reset, used
        );

        when(
                mockExecution.execute(any(HttpRequest.class), any(byte[].class))
        ).thenReturn(mockResponse);
        when(
                mockResponse.getHeaders()
        ).thenReturn(httpHeaders);

        return mockExecution;
    }

    @SuppressWarnings("checkstyle:parameternumber")
    private HttpHeaders getGithubRatelimitingHeaders(
            long limit, long remaining, long reset, long used
    ) {
        var httpHeaders = new HttpHeaders();
        httpHeaders.put("x-ratelimit-limit", List.of(Long.toString(limit)));
        httpHeaders.put("x-ratelimit-remaining", List.of(Long.toString(remaining)));
        httpHeaders.put("x-ratelimit-reset", List.of(Long.toString(reset)));
        httpHeaders.put("x-ratelimit-used", List.of(Long.toString(used)));
        return httpHeaders;
    }
}