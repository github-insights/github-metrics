package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.apistate.ApiRateLimitState;
import be.xplore.githubmetrics.domain.apistate.ApiRateLimitStatus;
import be.xplore.githubmetrics.githubadapter.cacheevicting.CacheEvictionProperties;
import be.xplore.githubmetrics.githubadapter.config.DebugInterceptor;
import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import be.xplore.githubmetrics.githubadapter.config.GithubRestClientConfig;
import be.xplore.githubmetrics.githubadapter.config.auth.GithubAuthTokenInterceptor;
import be.xplore.githubmetrics.githubadapter.config.auth.GithubJwtTokenInterceptor;
import be.xplore.githubmetrics.githubadapter.config.auth.GithubUnauthorizedInterceptor;
import be.xplore.githubmetrics.githubadapter.config.ratelimiting.RateLimitResetAwaitScheduler;
import be.xplore.githubmetrics.githubadapter.config.ratelimiting.RateLimitingInterceptor;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.mockito.Mockito.mock;

public class TestUtility {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestUtility.class);
    private static final DebugInterceptor DEBUG_INTERCEPTOR = new DebugInterceptor();

    public static CacheEvictionProperties getCacheEvictionProperties() {
        var state = new CacheEvictionProperties.EvictionState("", ApiRateLimitStatus.OK);
        return new CacheEvictionProperties(
                state, state, state, state, state, state
        );
    }

    public static RateLimitingInterceptor getRateLimitingInterceptor() {
        return new RateLimitingInterceptor(
                getNoAuthGithubProperties(8080),
                mock(RateLimitResetAwaitScheduler.class),
                getApiRateLimitState()
        );
    }

    public static ApiRateLimitState getApiRateLimitState() {
        return new ApiRateLimitState(0.9, 1.2, 0.9, 0.7, 0.5);
    }

    public static GithubAuthTokenInterceptor getAuthTokenInterceptor(GithubProperties githubProperties) {
        var restClientConfig = new GithubRestClientConfig(
                new GithubUnauthorizedInterceptor(), DEBUG_INTERCEPTOR, githubProperties, getRateLimitingInterceptor()
        );
        var jwtInterceptor = new GithubJwtTokenInterceptor(githubProperties);
        var jwtRestClient = restClientConfig.tokenFetcherRestClient(
                jwtInterceptor
        );
        jwtRestClient.mutate()
                .requestInterceptors(interceptors -> {
                    interceptors.add(jwtInterceptor);
                    interceptors.add(DEBUG_INTERCEPTOR);
                });
        return new GithubAuthTokenInterceptor(githubProperties, jwtRestClient);
    }

    public static RestClient getDefaultRestClientNoAuth(GithubProperties githubProperties) {
        return RestClient.builder()
                .baseUrl(githubProperties.url())
                .defaultHeaders(
                        httpHeaders -> {
                            httpHeaders.set("X-Github-Api-Version", "2022-11-28");
                            httpHeaders.set("Accept", "application/vnd.github+json");
                        }
                )
                .requestInterceptor(DEBUG_INTERCEPTOR)
                .build();
    }

    public static GithubProperties getNoAuthGithubProperties(int port) {
        return new GithubProperties(
                "http://localhost:" + port,
                "github-insights",
                new GithubProperties.Application(
                        "123",
                        "123456",
                        "pem-key"
                ),
                getRateLimitingProperties()
        );
    }

    public static GithubProperties getAuthGithubProperties(int port) {
        return new GithubProperties(
                "http://localhost:" + port,
                "github-insights",
                new GithubProperties.Application(
                        "123",
                        "123456",
                        validPemKey()
                ),
                getRateLimitingProperties()
        );
    }

    public static GithubProperties.RateLimiting getRateLimitingProperties() {
        return new GithubProperties.RateLimiting(
                60, 0.9, 1.2, 0.9, 0.7, 0.5
        );
    }

    public static String validPemKey() {
        return "-----BEGIN PRIVATE KEY-----\n" +
                "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCjknN/LTKuPvXU\n" +
                "bvLvyAwIs7I189CC+JErIOWwYzE1zRRLn7zNbG+UQJFqVorWm/84OqX/iC9HYudo\n" +
                "r4qFfT67Fynfs5/OMzbqp7ECX24oKF1G919bUHY8KrJY4CUJhgo4FOda3PuzbmEd\n" +
                "/POB6OcQ2vyJg1SsGY5REfheIaZ+AHVlTaze6HvejNJWE29iVIBxd0a4cWy9T4xt\n" +
                "DS5+Z9juoN+l8QvPjFaYenPzWgOqyw5Fsgkulv++M4A/htPX3iFWQNV1Mix9Y/aS\n" +
                "jbi8ft5+rlt6+llVT87fSgdeT9ZCmceIjwtZW+Zk6dlknXRoTI5q6PZfAFVDKhFN\n" +
                "RKdbSP03AgMBAAECggEAE2aCVlopAausAocqPzBN5RZTE70YRQBwT1o2g+Yv5v6s\n" +
                "4o9OmFq9HStPU/pxuySDb8rc92LSoLflVHBFkLGbKBuGNucaFB3U7J35C5v/97lZ\n" +
                "3tnmMHFppJc30fy7x9ZjDeXEzu0Y2V5FHIZs50KVbVVb0H+IImVhkNH90ERTd+wr\n" +
                "dbQ2lVg+lv86depJ5YhKs7iDbFnztx+OAdHrAo6qEff1gODv+9JK172pBmZsb+j6\n" +
                "l/8FSWJN57FfV61QuaKWezS3ASvxDWsOlI7QLuQwOzg4J4LioBsdE+Q3uHhkx6oF\n" +
                "f/hyYkexsnAl933C/VETK/9tjQf6nBHa29i1nq9k+QKBgQDM/EXj8UR7CJ9cINY4\n" +
                "QLuMDT4qPgIPjVccWJsv/roL43SYZ1NSs7FcfnRWQgCEtAClMXY8E6Q0zMiRRTu0\n" +
                "eQkHJabC0R8HZbF3AKj7zMwTj9X6qm4mondXsqJ0k+YqDlEz2fHXbABHAodL1nlu\n" +
                "wC52I+Nk+ZmLTBsVd1SFgnM8kwKBgQDMR7YheIf6Z8zwuDpbM09fCHXe5SnWSP3m\n" +
                "kN/WGvnTelvi1SUv8cn1+9bc17yJEF35M4i6Fracox+LlGfDJ8neKDJ0MlqKH7Jd\n" +
                "244YiXUaMWQbZcXLx052vwF/Vi7gVnFxdqCM7Nx/AV1vTN+Z7OH5JuQOM+qazR4a\n" +
                "YDYaSsFHTQKBgDtv7ugQXlX6gxLYpqT7CCas9FiVUE2oIxkiDCWXi+TEmFtUopF5\n" +
                "bzUtqZgVXUcdVo6P0APNgjCZLJMK6ywCaH69CSS2NHQVpaam91jD4mzNqTMc1gG3\n" +
                "3Dj+oCKDfBq3ug355SkctNviPM7dqqpVaWNyNo5h3YbJk5Te3BA2aimnAoGAEdqy\n" +
                "sHo4aEpqPx/a+d2iMkwrATBGV9RJXL2M0snIzBMFtO2sMmSPolBAl0zDzbcAf6dh\n" +
                "a+JQU6BuQWTXLNdtbV1WC5HbF/dtP4bRBJP/CCsI9NwQTZ893GMVXmvJ7RGhGKml\n" +
                "nquVGgSkhfXSFUH+/ifIBvXCq4UB/IwsLmAaRIECgYAeX/Lc1r2TWySP0ny+Ra+H\n" +
                "fUUh3gRV/FFEnVfTiyIcP3UL4ikPOCXrDaIE8QOPs5hUzzen5Vo2NG66LyzgH2HG\n" +
                "NAsWO4FEfeuh1jOn9Nz+FIvcATdB2GFYT7qRb44gNq9yWj7Dx9BFByLXht4XM1cH\n" +
                "n3lLUx3lpr32I+zijs25AQ==\n" +
                "-----END PRIVATE KEY-----";
    }

    public static WireMockServer getWireMockServer() {
        var wireMockServer = new WireMockServer(
                wireMockConfig().dynamicPort()
        );
        wireMockServer.start();
        configureFor("localhost", wireMockServer.port());
        LOGGER.info(
                "Wiremock Server running on {}.",
                wireMockServer.baseUrl()
        );
        return wireMockServer;
    }

    public static String yesterday() {
        return LocalDate.now().minusDays(1)
                .format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
