package be.xplore.githubmetrics.githubadapter.auth;

import be.xplore.githubmetrics.githubadapter.TestUtility;
import be.xplore.githubmetrics.githubadapter.config.DebugInterceptor;
import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import be.xplore.githubmetrics.githubadapter.config.auth.GithubUnauthorizedInterceptor;
import be.xplore.githubmetrics.githubadapter.exceptions.GithubRequestWasUnauthenticatedException;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GithubUnauthorizedInterceptorTest {
    private final GithubUnauthorizedInterceptor unauthorizedInterceptor = new GithubUnauthorizedInterceptor();
    private final DebugInterceptor debugInterceptor = new DebugInterceptor();
    private WireMockServer wireMockServer;
    private RestClient restClient;

    @BeforeEach
    void setUp() {
        this.wireMockServer = TestUtility.getWireMockServer();
        GithubProperties githubProperties = TestUtility.getNoAuthGithubProperties(
                this.wireMockServer.port()
        );
        this.restClient = RestClient.builder()
                .baseUrl(githubProperties.url())
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, this.unauthorizedInterceptor)
                .requestInterceptor(debugInterceptor)
                .build();
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void restClientWithUnauthorizedInterceptorShouldThrowOn401() {
        stubFor(
                get(urlEqualTo("/"))
                        .willReturn(
                                aResponse().withStatus(401)
                        )
        );
        var request = this.restClient.get().retrieve();
        assertThrows(
                GithubRequestWasUnauthenticatedException.class,
                request::toBodilessEntity
        );
    }

    @Test
    void restClientWithUnauthorizedInterceptorShouldThrowOn403() {
        stubFor(
                get(urlEqualTo("/"))
                        .willReturn(
                                aResponse().withStatus(403)
                        )
        );
        var request = this.restClient.get().retrieve();
        assertThrows(
                GithubRequestWasUnauthenticatedException.class,
                request::toBodilessEntity
        );
    }

    @Test
    void restClientWithUnauthorizedInterceptorShouldNotThrowOn404() {
        stubFor(
                get(urlEqualTo("/"))
                        .willReturn(
                                aResponse().withStatus(404)
                        )
        );
        var request = this.restClient.get().retrieve();
        assertDoesNotThrow(
                request::toBodilessEntity
        );
    }
}
