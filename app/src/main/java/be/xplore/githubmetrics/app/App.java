package be.xplore.githubmetrics.app;

import be.xplore.githubmetrics.domain.config.SchedulingConfig;
import be.xplore.githubmetrics.githubadapter.config.GithubConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "be.xplore.githubmetrics.*"
})
@EnableConfigurationProperties({GithubConfig.class, SchedulingConfig.class})
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class);
    }
}
