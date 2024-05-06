package be.xplore.githubmetrics.githubadapter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.github")
public record GithubConfig(
        String schema,
        String host,
        String port,
        String org,
        Application application

) {
    public record Application(
            String id,
            String installId,
            String pem
    ) {
    }
}
