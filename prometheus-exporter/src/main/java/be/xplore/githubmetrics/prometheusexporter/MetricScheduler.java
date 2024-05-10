package be.xplore.githubmetrics.prometheusexporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@ConditionalOnProperty(
        value = "app.scheduling.enable", havingValue = "true", matchIfMissing = true
)
@Service


public class MetricScheduler implements SchedulingConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricScheduler.class);
    private final Map<String, ScheduledFuture<?>> scheduledTasksMap = new HashMap<>();
    private final TaskScheduler taskScheduler;
    private final List<ScheduledExporter> scheduledExporters;

    public MetricScheduler(
            TaskScheduler taskScheduler,
            List<ScheduledExporter> scheduledExporters
    ) {
        this.taskScheduler = taskScheduler;
        this.scheduledExporters = scheduledExporters;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(5);
        threadPoolTaskScheduler.setThreadNamePrefix("scheduler-thread");
        threadPoolTaskScheduler.initialize();
        taskRegistrar.setTaskScheduler(threadPoolTaskScheduler);

        this.scheduledExporters.forEach(exporter ->
                this.addTask(exporter.getClass(), exporter::run, exporter.cronExpression())
        );
    }

    public void addTask(
            Class<? extends ScheduledExporter> key,
            Runnable task,
            String cronExp
    ) {
        LOGGER.info(
                "Adding task of class {} on this schedule {}",
                key.getSimpleName(), cronExp
        );
        this.scheduledTasksMap.put(
                key.getSimpleName(),
                this.taskScheduler.schedule(
                        task,
                        triggerContext -> new CronTrigger(cronExp)
                                .nextExecution(triggerContext)
                )
        );
    }

    public void cancelTask(Class<? extends ScheduledExporter> key) {
        ScheduledFuture<?> future = this.scheduledTasksMap.remove(key.getSimpleName());
        if (future == null) {
            LOGGER.error("Tried to cancel task of class that does not exist in map: {}", key.getSimpleName());
            return;
        }
        future.cancel(true);
    }

}

