package be.xplore.githubmetrics.githubadapter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.github")
public record GithubConfig(
        String host,
        String token,
        String org
) {
}
