package be.xplore.githubmetrics.githubadapter.auth;

import be.xplore.githubmetrics.githubadapter.TestUtility;
import be.xplore.githubmetrics.githubadapter.config.DebugInterceptor;
import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import be.xplore.githubmetrics.githubadapter.config.GithubRestClientConfig;
import be.xplore.githubmetrics.githubadapter.config.auth.GithubAuthTokenInterceptor;
import be.xplore.githubmetrics.githubadapter.config.auth.GithubJwtTokenInterceptor;
import be.xplore.githubmetrics.githubadapter.config.auth.GithubUnauthorizedInterceptor;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.Instant;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GithubAuthTokenInterceptorTest {
    private final WireMockServer wireMockServer = TestUtility.getWireMockServer();
    private final String token = "my_special_token";
    private final String tokenRequestResponse = MessageFormat.format(
            "'{'\"token\": \"{0}\", \"expires_at\": \"{1}\"'}'",
            token,
            Instant.now().plusSeconds(100)
    );
    private final GithubProperties githubProperties = TestUtility.getAuthGithubProperties(
            wireMockServer.port()
    );
    private final GithubRestClientConfig restClientConfig = new GithubRestClientConfig(
            new GithubUnauthorizedInterceptor(),
            new DebugInterceptor(),
            githubProperties
    );
    private GithubAuthTokenInterceptor authTokenInterceptor;
    private HttpRequest mockHttpRequest;
    private HttpHeaders mockHttpHeaders;

    @BeforeEach
    void setUp() {
        GithubJwtTokenInterceptor jwtTokenInterceptor = new GithubJwtTokenInterceptor(this.githubProperties);
        this.authTokenInterceptor = new GithubAuthTokenInterceptor(
                this.githubProperties,
                this.restClientConfig.tokenFetcherRestClient(
                        jwtTokenInterceptor
                )
        );

        this.mockHttpRequest = mock(HttpRequest.class);
        this.mockHttpHeaders = mock(HttpHeaders.class);
        when(mockHttpRequest.getHeaders()).thenReturn(mockHttpHeaders);
    }

    @Test
    void tokenInterceptorShouldSetBearerAuthInRequestHeaders() throws IOException {

        stubFor(
                WireMock.post(urlEqualTo(
                        "/app/installations/" + githubProperties.application().installId() + "/access_tokens"
                )).willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("GithubAuthorizationResponse.json")));

        this.authTokenInterceptor.intercept(
                this.mockHttpRequest,
                new byte[1],
                mock(ClientHttpRequestExecution.class)
        );

        verify(mockHttpHeaders).setBearerAuth(anyString());
    }

    @Test
    void authTokenWithExpirationInFutureShouldBeReused() throws IOException {

        stubFor(
                WireMock.post(urlEqualTo(
                        "/app/installations/" + this.githubProperties.application().installId() + "/access_tokens"
                )).willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(tokenRequestResponse)));

        this.authTokenInterceptor.intercept(
                this.mockHttpRequest,
                new byte[1],
                mock(ClientHttpRequestExecution.class)
        );
        this.authTokenInterceptor.intercept(
                this.mockHttpRequest,
                new byte[1],
                mock(ClientHttpRequestExecution.class)
        );

        verify(mockHttpHeaders, times(2)).setBearerAuth(token);
    }

    @Test
    void invalidAuthTokenResponseBodyShouldThrowException() {
        stubFor(
                WireMock.post(urlEqualTo(
                        "/app/installations/" + githubProperties.application().installId() + "/access_tokens"
                )).willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("")));

        assertThrows(
                NullPointerException.class,
                () -> this.authTokenInterceptor.intercept(
                        this.mockHttpRequest,
                        new byte[1],
                        mock(ClientHttpRequestExecution.class)
                )
        );

    }
}
