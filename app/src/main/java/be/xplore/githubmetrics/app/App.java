package be.xplore.githubmetrics.app;

import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import be.xplore.githubmetrics.prometheusexporter.SchedulingProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "be.xplore.githubmetrics.*"
})
@EnableConfigurationProperties({GithubProperties.class, SchedulingProperties.class})
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class);
    }
}
