package be.xplore.githubmetrics.prometheusexporter.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;

@ConditionalOnProperty(
        value = "app.scheduling.exporters.enable", havingValue = "true", matchIfMissing = true
)
@Configuration
@EnableScheduling
public class ExporterSchedulingConfig {

    @Bean
    TaskScheduler prometheusExporterTaskScheduler() {
        var scheduler = new SimpleAsyncTaskScheduler();
        scheduler.setVirtualThreads(true);
        return scheduler;
    }
}
