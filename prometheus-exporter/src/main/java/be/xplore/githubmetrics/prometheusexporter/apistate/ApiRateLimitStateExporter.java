package be.xplore.githubmetrics.prometheusexporter.apistate;

import be.xplore.githubmetrics.domain.apistate.ApiRateLimitState;
import be.xplore.githubmetrics.domain.apistate.ApiRateLimitStatus;
import be.xplore.githubmetrics.domain.apistate.GetCurrentApiRateLimitStateUseCase;
import be.xplore.githubmetrics.prometheusexporter.ScheduledExporter;
import be.xplore.githubmetrics.prometheusexporter.config.SchedulingProperties;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ApiRateLimitStateExporter implements ScheduledExporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiRateLimitStateExporter.class);

    private static final String LIMIT = "api_ratelimit_state_limit";
    private static final String REMAINING = "api_ratelimit_state_remaining";
    private static final String RESET = "api_ratelimit_state_reset";
    private static final String USED = "api_ratelimit_state_used";
    private static final String STATUS = "api_ratelimit_state_status";
    private static final String PAUSED = "api_ratelimit_state_paused";
    private static final String ACTUAL = "api_ratelimit_state_actual_req";
    private static final String IDEAL = "api_ratelimit_state_ideal_req";

    private final String cronExpression;
    private final GetCurrentApiRateLimitStateUseCase getCurrentApiRateLimitStateUseCase;
    private final Map<String, AtomicLong> rateLimitStateGauges = new HashMap<>();

    public ApiRateLimitStateExporter(
            GetCurrentApiRateLimitStateUseCase getCurrentApiRateLimitStateUseCase,
            SchedulingProperties schedulingProperties,
            MeterRegistry registry
    ) {
        this.getCurrentApiRateLimitStateUseCase = getCurrentApiRateLimitStateUseCase;
        this.cronExpression = schedulingProperties.apiRateLimitState();

        this.initRateLimitGauges(registry);
    }

    private void retrieveAndExportApiRateLimitState() {
        LOGGER.trace("ApiRateLimitState scheduled task is running.");
        var currentState = this.getCurrentApiRateLimitStateUseCase.getCurrentApiRateLimitState();
        this.updateHeaderData(currentState);
        this.updateStatusData(currentState);
        this.updateRequestsPerSecData(currentState);
        LOGGER.trace("ApiRateLimitState scheduled task is finished.");
    }

    private void updateHeaderData(ApiRateLimitState state) {
        put(LIMIT, state.getLimit());
        put(REMAINING, state.getRemaining());
        put(RESET, state.getReset());
        put(USED, state.getUsed());
    }

    private void updateStatusData(ApiRateLimitState state) {
        var statusVal = state.getStatus().orElse(ApiRateLimitStatus.CRITICAL).ordinal();
        var paused = state.getStatus().map(status -> 0.0).orElse(1.0);
        put(STATUS, statusVal);
        put(PAUSED, paused);
    }

    private void updateRequestsPerSecData(ApiRateLimitState state) {
        put(ACTUAL, state.getActualRequestsPerSecond());
        put(IDEAL, state.getIdealRequestsPerSecond());
    }

    private void put(String name, double newVal) {
        this.rateLimitStateGauges.get(name)
                .set(Double.doubleToLongBits(newVal));
    }

    private void initRateLimitGauges(MeterRegistry registry) {
        var metrics = List.of(
                LIMIT, REMAINING, RESET, USED, STATUS, PAUSED, ACTUAL, IDEAL
        );
        metrics.forEach(metricName -> {
            var atomicLong = new AtomicLong();
            Gauge.builder(
                            metricName, () -> Double.longBitsToDouble(atomicLong.get())
                    ).strongReference(true)
                    .register(registry);
            this.rateLimitStateGauges.put(metricName, atomicLong);
        });
    }

    @Override
    public void run() {
        this.retrieveAndExportApiRateLimitState();
    }

    @Override
    public String cronExpression() {
        return this.cronExpression;
    }
}
