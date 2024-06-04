package be.xplore.githubmetrics.domain;

import be.xplore.githubmetrics.domain.apistate.ApiRateLimitState;
import be.xplore.githubmetrics.domain.apistate.ApiRateLimitStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiStateTests {
    private static final double RATE_LIMIT_BUFFER = 0.9;
    private static final double CRITICAL_LIMIT = 1.2;
    private static final double WARNING_LIMIT = 0.9;
    private static final double CONCERNING_LIMIT = 0.7;
    private static final double GOOD_LIMIT = 0.5;

    private ApiRateLimitState state;

    @BeforeEach
    void setUp() {
        state = new ApiRateLimitState(
                RATE_LIMIT_BUFFER, CRITICAL_LIMIT, WARNING_LIMIT,
                CONCERNING_LIMIT, GOOD_LIMIT
        );
    }

    private ApiRateLimitStatus getStatus() {
        return this.state.getStatus().get();
    }

    private ApiRateLimitStatus getAbsNewStatus() {
        return this.state.getNewAbsoluteStatus();
    }

    @Test
    void drasticRequestRatioWorseningShouldOnlyWorsenStatusByOne() {
        this.state.recalculateStatus(2, 1);
        assertEquals(ApiRateLimitStatus.GOOD, getStatus());
    }

    @Test
    void consistentRatioWorseningShouldSlowlyIncreaseStatus() {
        this.state.recalculateStatus(2, 1);
        assertEquals(ApiRateLimitStatus.GOOD, getStatus());
        this.state.recalculateStatus(2, 1);
        assertEquals(ApiRateLimitStatus.CONCERNING, getStatus());
        this.state.recalculateStatus(2, 1);
        assertEquals(ApiRateLimitStatus.WARNING, getStatus());
        this.state.recalculateStatus(2, 1);
        assertEquals(ApiRateLimitStatus.CRITICAL, getStatus());
    }

    @Test
    void actualToIdealRatioThatDoesntExceedGoodLimitShouldStayOk() {
        this.state.recalculateStatus(GOOD_LIMIT, 1);
        assertEquals(ApiRateLimitStatus.OK, getAbsNewStatus());
    }

    @Test
    void actualToIdealRatioThatDoesntExceedConcerningLimitShouldBecomeOk() {
        this.state.recalculateStatus(CONCERNING_LIMIT, 1);
        assertEquals(ApiRateLimitStatus.GOOD, getAbsNewStatus());
    }

    @Test
    void actualToIdealRatioThatDoesntExceedWarningLimitShouldBecomeConcerning() {
        this.state.recalculateStatus(WARNING_LIMIT, 1);
        assertEquals(ApiRateLimitStatus.CONCERNING, getAbsNewStatus());
    }

    @Test
    void actualToIdealRatioThatDoesntExceedCriticalLimitShouldBecomeConcerning() {
        this.state.recalculateStatus(CRITICAL_LIMIT, 1);
        assertEquals(ApiRateLimitStatus.WARNING, getAbsNewStatus());
    }

    @Test
    void actualToIdealRatioThatExceedsCriticalLimitShouldBecomeCritical() {
        this.state.recalculateStatus(2, 1);
        assertEquals(ApiRateLimitStatus.CRITICAL, getAbsNewStatus());
    }

    @Test
    void drasticRequestRatioImprovementShouldOnlyImproveStatusByOne() {
        this.state.recalculateStatus(2, 1);
        this.state.recalculateStatus(2, 1);
        this.state.recalculateStatus(2, 1);
        this.state.recalculateStatus(2, 1);
        assertEquals(ApiRateLimitStatus.CRITICAL, getStatus());
        this.state.recalculateStatus(GOOD_LIMIT, 1);
        assertEquals(ApiRateLimitStatus.WARNING, getStatus());
    }

    @Test
    void stopRequestsAndGetResetFunctionShouldClearStatusAndAllowForReset() {
        assertTrue(this.state.getStatus().isPresent());
        Runnable resetFn = this.state.stopRequestsAndGetResetFunction();
        assertTrue(this.state.getStatus().isEmpty());
        resetFn.run();
        assertEquals(ApiRateLimitStatus.WARNING, getStatus());
    }

    @Test
    void completelyStoppingRequestingShouldAlwaysCauseDataWaitToReturnTrue() {
        this.state.stopRequestsAndGetResetFunction();

        shouldWaitAndRaise(ApiRateLimitStatus.GOOD);
        shouldWaitAndRaise(ApiRateLimitStatus.CONCERNING);
        shouldWaitAndRaise(ApiRateLimitStatus.WARNING);
        shouldWaitAndRaise(ApiRateLimitStatus.CONCERNING);
        shouldWaitAndRaise(null);
    }

    @Test
    void dataShouldWaitWhenActualStatusIsOnSameLevelAsLimit() {
        shouldWaitAndRaise(ApiRateLimitStatus.OK);
        shouldWaitAndRaise(ApiRateLimitStatus.GOOD);
        shouldWaitAndRaise(ApiRateLimitStatus.CONCERNING);
        shouldWaitAndRaise(ApiRateLimitStatus.WARNING);
        shouldWaitAndRaise(ApiRateLimitStatus.CRITICAL);
    }

    @Test
    void nullLimitShouldNeverAskDataToWait() {
        shouldNotWaitAndRaise(null);
        shouldNotWaitAndRaise(null);
        shouldNotWaitAndRaise(null);
        shouldNotWaitAndRaise(null);
        shouldNotWaitAndRaise(null);
    }

    private void shouldNotWaitAndRaise(ApiRateLimitStatus status) {
        assertFalse(this.state.shouldDataWait(status));
        this.state.raiseStatusByOne();
    }

    private void shouldWaitAndRaise(ApiRateLimitStatus status) {
        assertTrue(this.state.shouldDataWait(status));
        this.state.raiseStatusByOne();
    }
}