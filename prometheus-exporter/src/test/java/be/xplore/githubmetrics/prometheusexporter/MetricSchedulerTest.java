package be.xplore.githubmetrics.prometheusexporter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MetricSchedulerTest {

    private static final String CRON_EXP = "0 */1 * * * ?";
    private final TaskScheduler mockScheduler = mock(TaskScheduler.class);
    private final ScheduledFuture mockFuture = mock(ScheduledFuture.class);
    private final ScheduledExporter mockExporter = mock(ScheduledExporter.class);
    private MetricScheduler metricScheduler;

    @BeforeEach
    void setUp() {
        when(this.mockScheduler.schedule(any(Runnable.class), any(Trigger.class)))
                .thenReturn(this.mockFuture);
        when(this.mockExporter.cronExpression()).thenReturn(CRON_EXP);

        this.metricScheduler = new MetricScheduler(this.mockScheduler, List.of(this.mockExporter));
        this.metricScheduler.configureTasks(mock(ScheduledTaskRegistrar.class));
    }

    @Test
    void initializingTheSchedulerWithOneExporterInTheListShouldScheduleOneTask() {
        verify(this.mockExporter).cronExpression();
        verify(this.mockScheduler).schedule(
                any(Runnable.class),
                any(Trigger.class)
        );
    }

    @Test
    void callingCancelTaskShouldCancelFuture() {
        this.metricScheduler.cancelTask(this.mockExporter.getClass());
        verify(this.mockFuture).cancel(true);

    }

    @Test
    void callingCancelTaskForInexistentClassShouldNotThrow() {
        assertDoesNotThrow(
                () -> this.metricScheduler.cancelTask(ScheduledExporter.class)
        );
    }
}
