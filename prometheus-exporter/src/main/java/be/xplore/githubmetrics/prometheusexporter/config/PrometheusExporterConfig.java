package be.xplore.githubmetrics.prometheusexporter.config;

import be.xplore.githubmetrics.prometheusexporter.features.Features;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.manager.EnumBasedFeatureProvider;
import org.togglz.core.spi.FeatureProvider;

@Configuration
@EnableConfigurationProperties({SchedulingProperties.class})
public class PrometheusExporterConfig {
    @Bean
    public FeatureProvider featureProvider() {
        return new EnumBasedFeatureProvider(Features.class);
    }
}
