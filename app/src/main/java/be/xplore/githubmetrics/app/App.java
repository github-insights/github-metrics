package be.xplore.githubmetrics.app;

import be.xplore.githubmetrics.githubadapter.config.GithubConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {
        "be.xplore.githubmetrics.*"
})
@EnableConfigurationProperties(GithubConfig.class)
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class);
    }
}
