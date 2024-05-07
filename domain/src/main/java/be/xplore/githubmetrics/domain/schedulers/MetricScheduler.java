package be.xplore.githubmetrics.domain.schedulers;

import be.xplore.githubmetrics.domain.config.SchedulingConfig;
import be.xplore.githubmetrics.domain.schedulers.ports.JobsUseCase;
import be.xplore.githubmetrics.domain.schedulers.ports.WorkflowRunsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@ConditionalOnProperty(
        value = "app.scheduling.enable", havingValue = "true", matchIfMissing = true
)
@Component
@EnableScheduling
public class MetricScheduler implements SchedulingConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricScheduler.class);
    private final WorkflowRunsUseCase workflowRunsUseCase;
    private final JobsUseCase jobsUseCase;
    private final SchedulingConfig schedulingConfig;
    private final Map<String, ScheduledFuture<?>> workflowRunsJob = new HashMap<>();

    public MetricScheduler(WorkflowRunsUseCase workflowRunsUseCase, JobsUseCase jobsUseCase, SchedulingConfig schedulingConfig) {
        this.workflowRunsUseCase = workflowRunsUseCase;
        this.jobsUseCase = jobsUseCase;
        this.schedulingConfig = schedulingConfig;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(5);
        threadPoolTaskScheduler.setThreadNamePrefix("scheduler-thread");
        threadPoolTaskScheduler.initialize();
        workflowRunsJob(threadPoolTaskScheduler);
        jobsJob(threadPoolTaskScheduler);
        taskRegistrar.setTaskScheduler(threadPoolTaskScheduler);
    }

    private void workflowRunsJob(org.springframework.scheduling.TaskScheduler taskScheduler) {
        this.workflowRunsJob.put("WorkFlowRunsJob", taskScheduler.schedule(
                workflowRunsUseCase::retrieveAndExportWorkflowRuns,
                triggerContext -> {
                    String cronExp = schedulingConfig.workflowRunsInterval();
                    LOGGER.warn("WorkflowRunsJob cron: {}", cronExp);
                    return new CronTrigger(cronExp).nextExecution(triggerContext);
                }));
    }

    private void jobsJob(org.springframework.scheduling.TaskScheduler taskScheduler) {
        this.workflowRunsJob.put("JobsJob", taskScheduler.schedule(
                jobsUseCase::retrieveAndExportJobs,
                triggerContext -> {
                    String cronExp = schedulingConfig.jobsInterval();
                    LOGGER.warn("JobsJob cron: {}", cronExp);
                    return new CronTrigger(cronExp).nextExecution(triggerContext);
                }));
    }

    private void cancelScheduledTask(String key) {
        ScheduledFuture<?> future = this.workflowRunsJob.get(key);
        if (future != null) {
            future.cancel(true);
        }
    }

}

