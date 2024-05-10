package be.xplore.githubmetrics.prometheusexporter.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({SchedulingProperties.class})
public class PrometheusExporterConfig {
}
