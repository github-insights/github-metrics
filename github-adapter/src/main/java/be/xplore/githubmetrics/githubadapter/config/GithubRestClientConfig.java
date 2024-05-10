package be.xplore.githubmetrics.githubadapter.config;

import be.xplore.githubmetrics.githubadapter.config.auth.GithubAuthTokenInterceptor;
import be.xplore.githubmetrics.githubadapter.config.auth.GithubJwtTokenInterceptor;
import be.xplore.githubmetrics.githubadapter.config.auth.GithubUnauthorizedInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

@Configuration
public class GithubRestClientConfig {

    private final GithubUnauthorizedInterceptor unauthorizedInterceptor;

    public GithubRestClientConfig(GithubUnauthorizedInterceptor unauthorizedInterceptor) {
        this.unauthorizedInterceptor = unauthorizedInterceptor;
    }

    @Bean
    public RestClient defaultRestClient(
            GithubAuthTokenInterceptor githubAuthTokenInterceptor,
            GithubProperties githubProperties
    ) {
        return RestClient.builder()
                .baseUrl(githubProperties.url())
                .defaultHeaders(
                        httpHeaders -> {
                            httpHeaders.set("X-Github-Api-Version", "2022-11-28");
                            httpHeaders.set("Accept", "application/vnd.github+json");
                        }
                )
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, this.unauthorizedInterceptor)
                .requestInterceptor(githubAuthTokenInterceptor)
                .build();
    }

    @Bean
    public RestClient tokenFetcherRestClient(
            GithubJwtTokenInterceptor githubJwtTokenInterceptor,
            GithubProperties githubProperties
    ) {
        return RestClient.builder()
                .baseUrl(githubProperties.url())
                .defaultHeaders(
                        httpHeaders -> {
                            httpHeaders.set("X-Github-Api-Version", "2022-11-28");
                            httpHeaders.set("Accept", "application/vnd.github+json");
                        }
                )
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, this.unauthorizedInterceptor)
                .requestInterceptor(githubJwtTokenInterceptor)
                .build();
    }
}