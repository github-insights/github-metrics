package be.xplore.githubmetrics;

import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import be.xplore.githubmetrics.prometheusexporter.SchedulingProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({GithubProperties.class, SchedulingProperties.class})
public class GithubMetrics {

    public static void main(String[] args) {
        SpringApplication.run(GithubMetrics.class);
    }
}
