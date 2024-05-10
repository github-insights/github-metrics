package be.xplore.githubmetrics.githubadapter.config.auth;

import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import be.xplore.githubmetrics.githubadapter.mappingclasses.GHAppInstallationAccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.ZonedDateTime;

@Component
public class GithubAuthTokenInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(GithubAuthTokenInterceptor.class);
    private final GithubProperties githubProperties;
    private final RestClient tokenFetcherRestClient;
    private ZonedDateTime tokenExpirationDateTime = now();
    private String currentAccessToken;

    public GithubAuthTokenInterceptor(GithubProperties githubProperties, RestClient tokenFetcherRestClient) {
        this.githubProperties = githubProperties;
        this.tokenFetcherRestClient = tokenFetcherRestClient;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders().setBearerAuth(this.getAuthToken());
        return execution.execute(request, body);
    }

    private String getAuthToken() {
        LOGGER.debug("Getting Auth Header From Installation id + app id + app private key");

        if (now().isAfter(this.tokenExpirationDateTime)) {
            LOGGER.debug("Creating a new Access Token since the old one expired.");
            var accessToken = this.tokenFetcherRestClient.post()
                    .uri(this.getAccessTokenApiPath())
                    .retrieve()
                    .body(GHAppInstallationAccessToken.class);
            this.currentAccessToken = accessToken.token();
            this.tokenExpirationDateTime = accessToken.getActualDate();
        }

        return this.currentAccessToken;
    }

    private String getAccessTokenApiPath() {
        return MessageFormat.format(
                "/app/installations/{0}/access_tokens",
                githubProperties.application().installId()
        );
    }

    private ZonedDateTime now() {
        return ZonedDateTime.now();
    }
}