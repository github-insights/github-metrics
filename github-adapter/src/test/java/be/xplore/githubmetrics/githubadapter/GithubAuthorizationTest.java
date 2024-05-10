package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import be.xplore.githubmetrics.githubadapter.config.auth.GithubJwtTokenInterceptor;
import be.xplore.githubmetrics.githubadapter.exceptions.UnableToAuthenticateGithubAppException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GithubAuthorizationTest {

    private GithubProperties githubProperties;
    private WireMockServer wireMockServer;

    @BeforeEach
    void setupWireMock() {
        wireMockServer = TestUtility.getWireMockServer();
        githubProperties = TestUtility.getAuthGithubProperties(wireMockServer.port());
    }

    @Test
    void invalidPemKeyShouldLeadToExceptionThrown() {
        githubProperties = TestUtility.getNoAuthGithubProperties(wireMockServer.port());
        var jwtInterceptor = new GithubJwtTokenInterceptor(githubProperties);
        var mockHttpHeaders = mock(HttpRequest.class);
        when(mockHttpHeaders.getHeaders()).thenReturn(mock(HttpHeaders.class));

        assertThrows(
                UnableToAuthenticateGithubAppException.class,
                () -> jwtInterceptor.intercept(
                        mockHttpHeaders,
                        new byte[1],
                        mock(ClientHttpRequestExecution.class)
                )
        );
    }

    @Test
    void invalidAuthTokenResponseBodyShouldThrowException() {
        var tokenInterceptor = TestUtility.getAuthTokenInterceptor(this.githubProperties);

        stubFor(
                WireMock.post(urlEqualTo(
                        "/app/installations/" + githubProperties.application().installId() + "/access_tokens"
                )).willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("")));

        var mockHttpHeaders = TestUtility.mockHttpRequestWithMockedHeaders();

        assertThrows(
                NullPointerException.class,
                () -> tokenInterceptor.intercept(
                        mockHttpHeaders,
                        new byte[1],
                        mock(ClientHttpRequestExecution.class)
                )
        );

    }

    @Test
    void tokenInterceptorShouldSetBearerAuthInRequestHeaders() throws IOException {
        var tokenInterceptor = TestUtility.getAuthTokenInterceptor(this.githubProperties);

        stubFor(
                WireMock.post(urlEqualTo(
                        "/app/installations/" + githubProperties.application().installId() + "/access_tokens"
                )).willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("GithubAuthorizationResponse.json")));

        var mockHttpRequest = mock(HttpRequest.class);
        var mockHttpHeaders = mock(HttpHeaders.class);
        when(mockHttpRequest.getHeaders()).thenReturn(mockHttpHeaders);

        tokenInterceptor.intercept(
                mockHttpRequest,
                new byte[1],
                mock(ClientHttpRequestExecution.class)
        );

        verify(mockHttpHeaders).setBearerAuth(anyString());
    }
}