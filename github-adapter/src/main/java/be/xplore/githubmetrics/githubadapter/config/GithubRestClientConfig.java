package be.xplore.githubmetrics.githubadapter.config;

import be.xplore.githubmetrics.githubadapter.config.auth.GithubAuthTokenInterceptor;
import be.xplore.githubmetrics.githubadapter.config.auth.GithubJwtTokenInterceptor;
import be.xplore.githubmetrics.githubadapter.config.auth.GithubUnauthorizedInterceptor;
import be.xplore.githubmetrics.githubadapter.config.ratelimiting.RateLimitingInterceptor;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

@Configuration
public class GithubRestClientConfig {

    private final GithubUnauthorizedInterceptor unauthorizedInterceptor;
    private final DebugInterceptor debugInterceptor;
    private final GithubProperties githubProperties;
    private final ObservationRegistry observationRegistry;
    private final GithubRestClientRequestObservationConvention githubRestClientRequestObservationConvention;

    private final Duration timeout = Duration.ofSeconds(30L);

    public GithubRestClientConfig(
            GithubUnauthorizedInterceptor unauthorizedInterceptor,
            DebugInterceptor debugInterceptor,
            GithubProperties githubProperties,
            ObservationRegistry observationRegistry,
            GithubRestClientRequestObservationConvention githubRestClientRequestObservationConvention
    ) {
        this.unauthorizedInterceptor = unauthorizedInterceptor;
        this.debugInterceptor = debugInterceptor;
        this.githubProperties = githubProperties;
        this.observationRegistry = observationRegistry;
        this.githubRestClientRequestObservationConvention = githubRestClientRequestObservationConvention;
    }

    @Bean
    @Primary
    public GithubRestClient defaultGithubRestClient(
            GithubAuthTokenInterceptor githubAuthTokenInterceptor,
            RateLimitingInterceptor rateLimitingInterceptor,
            GenericConversionService conversionService
    ) {
        return getGithubRestClient(
                List.of(githubAuthTokenInterceptor, rateLimitingInterceptor),
                conversionService
        );
    }

    @Bean
    public GithubRestClient githubAuthRestClient(
            GithubJwtTokenInterceptor githubJwtTokenInterceptor,
            GenericConversionService conversionService
    ) {
        return this.getGithubRestClient(
                List.of(githubJwtTokenInterceptor),
                conversionService
        );
    }

    @SuppressWarnings("PMD.CloseResource")
    public GithubRestClient getGithubRestClient(
            List<ClientHttpRequestInterceptor> interceptorsToAdd,
            GenericConversionService conversionService
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(this.timeout).build();
        JdkClientHttpRequestFactory requestFactory
                = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(this.timeout);

        var restClient = RestClient.builder()
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
                .requestInterceptors(
                        interceptors -> {
                            interceptors.addAll(interceptorsToAdd);
                            interceptors.add(this.debugInterceptor);
                        }).build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .conversionService(conversionService)
                .build();
        return factory.createClient(GithubRestClient.class);
    }
}