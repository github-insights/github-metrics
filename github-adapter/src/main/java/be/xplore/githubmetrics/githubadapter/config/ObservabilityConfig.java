package be.xplore.githubmetrics.githubadapter.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObservabilityConfig {
    @Bean
    ObservationRegistry observationRegistry() {
        return ObservationRegistry.create();
    }
}
