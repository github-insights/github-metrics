package be.xplore.githubmetrics.githubadapter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class GithubRestClientConfiguration {

    @Bean
    public RestClient getGithubRestClient(GithubConfig githubConfig) {

        return RestClient.builder()
                .defaultHeaders(
                        httpHeaders -> {
                            httpHeaders.set("X-Github-Api-Version", "2022-11-28");
                            httpHeaders.set("Accept", "application/vnd.github+json");
                        }
                )
                .build();
    }
}
