package be.xplore.githubmetrics.githubadapter.auth;

import be.xplore.githubmetrics.githubadapter.TestUtility;
import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import be.xplore.githubmetrics.githubadapter.config.auth.GithubJwtTokenInterceptor;
import be.xplore.githubmetrics.githubadapter.exceptions.UnableToAuthenticateGithubAppException;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GithubJwtTokenInterceptorTest {
    private GithubProperties githubProperties;

    @BeforeEach
    void setupWireMock() {
        WireMockServer wireMockServer = TestUtility.getWireMockServer();
        githubProperties = TestUtility.getNoAuthGithubProperties(wireMockServer.port());
    }

    @Test
    void invalidPemKeyShouldLeadToExceptionThrown() {
        var jwtInterceptor = new GithubJwtTokenInterceptor(this.githubProperties);
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
}
