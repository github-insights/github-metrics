package be.xplore.githubmetrics.githubadapter.cacheevicting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

@ConditionalOnProperty(
        value = "app.scheduling.cacheeviction.enable", havingValue = "true", matchIfMissing = true
)
@Service
public class EvictionScheduler implements SchedulingConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(EvictionScheduler.class);
    private final Map<String, ScheduledFuture<?>> scheduledTasksMap = new HashMap<>();
    private final TaskScheduler taskScheduler;
    private final CacheManager cacheManager;
    private final List<ScheduledCacheEvictionPort> cacheEvictionPorts;

    public EvictionScheduler(
            @Qualifier("githubAdapterTaskScheduler") TaskScheduler taskScheduler,
            CacheManager cacheManager,
            List<ScheduledCacheEvictionPort> cacheEvictionPorts
    ) {
        this.taskScheduler = taskScheduler;
        this.cacheManager = cacheManager;
        this.cacheEvictionPorts = cacheEvictionPorts;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(5);
        threadPoolTaskScheduler.setThreadNamePrefix("cache-eviction-scheduler");
        threadPoolTaskScheduler.initialize();

        this.cacheEvictionPorts.forEach(port -> {
            LOGGER.trace(
                    "Adding scheduled eviction task for {} on startup",
                    port.cacheName()
            );
            this.addTask(port.getClass(), () -> this.conditionallyCacheEvict(port), port.cronExpression());
        });
    }

    private void conditionallyCacheEvict(ScheduledCacheEvictionPort port) {
        var name = port.cacheName();

        if (!port.freshDataCanWait()) {
            LOGGER.debug("Evicting {}", name);
            Optional.ofNullable(
                    this.cacheManager.getCache(name)
            ).ifPresent(Cache::clear);
        } else {
            LOGGER.debug("Did not evict {}", name);
        }
    }

    public void addTask(
            Class<? extends ScheduledCacheEvictionPort> key,
            Runnable task,
            String cronExp
    ) {
        LOGGER.info(
                "Adding cache eviction task of class {} on this schedule {}",
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

    public void cancelTask(Class<? extends ScheduledCacheEvictionPort> key) {
        ScheduledFuture<?> future = this.scheduledTasksMap.remove(key.getSimpleName());
        if (future == null) {
            LOGGER.error("Tried to cancel task of class that does not exist in map: {}", key.getSimpleName());
            return;
        }
        future.cancel(true);
    }
}