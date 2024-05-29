package be.xplore.githubmetrics.githubadapter.config;

import be.xplore.githubmetrics.githubadapter.config.auth.GithubAuthTokenInterceptor;
import be.xplore.githubmetrics.githubadapter.config.auth.GithubJwtTokenInterceptor;
import be.xplore.githubmetrics.githubadapter.config.auth.GithubUnauthorizedInterceptor;
import be.xplore.githubmetrics.githubadapter.config.ratelimiting.RateLimitingInterceptor;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

@Configuration
public class GithubRestClientConfig {

    private final GithubUnauthorizedInterceptor unauthorizedInterceptor;
    private final DebugInterceptor debugInterceptor;
    private final GithubProperties githubProperties;
    private final RateLimitingInterceptor rateLimitingInterceptor;
    private final ObservationRegistry observationRegistry;
    private final GithubRestClientRequestObservationConvention githubRestClientRequestObservationConvention;

    public GithubRestClientConfig(
            GithubUnauthorizedInterceptor unauthorizedInterceptor,
            DebugInterceptor debugInterceptor,
            GithubProperties githubProperties,
            RateLimitingInterceptor rateLimitingInterceptor,
            ObservationRegistry observationRegistry,
            GithubRestClientRequestObservationConvention githubRestClientRequestObservationConvention
    ) {
        this.unauthorizedInterceptor = unauthorizedInterceptor;
        this.debugInterceptor = debugInterceptor;
        this.githubProperties = githubProperties;
        this.rateLimitingInterceptor = rateLimitingInterceptor;
        this.observationRegistry = observationRegistry;
        this.githubRestClientRequestObservationConvention = githubRestClientRequestObservationConvention;
    }

    @Bean
    public RestClient defaultRestClient(
            GithubAuthTokenInterceptor githubAuthTokenInterceptor
    ) {
        return this.getBasicRestClient()
                .requestInterceptors(interceptors -> {
                    interceptors.add(githubAuthTokenInterceptor);
                    interceptors.add(rateLimitingInterceptor);
                })
                .build();
    }

    @Bean
    public RestClient tokenFetcherRestClient(
            GithubJwtTokenInterceptor githubJwtTokenInterceptor
    ) {
        return this.getBasicRestClient()
                .requestInterceptors(interceptors ->
                        interceptors.add(githubJwtTokenInterceptor)
                )
                .build();
    }

    private RestClient.Builder getBasicRestClient() {
        return RestClient.builder()
                .baseUrl(githubProperties.url())
                .observationRegistry(this.observationRegistry)
                .observationConvention(this.githubRestClientRequestObservationConvention)
                .defaultHeaders(
                        httpHeaders -> {
                            httpHeaders.set("X-Github-Api-Version", "2022-11-28");
                            httpHeaders.set("Accept", "application/vnd.github+json");
                        }
                )
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, this.unauthorizedInterceptor)
                .requestInterceptors(interceptors ->
                        interceptors.add(debugInterceptor)
                );
    }
}