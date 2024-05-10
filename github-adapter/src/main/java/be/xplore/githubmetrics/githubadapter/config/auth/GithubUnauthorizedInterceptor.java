package be.xplore.githubmetrics.githubadapter.config.auth;

import be.xplore.githubmetrics.githubadapter.exceptions.GithubRequestWasUnauthenticatedException;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.text.MessageFormat;

@Component
public class GithubUnauthorizedInterceptor implements RestClient.ResponseSpec.ErrorHandler {

    @Override
    public void handle(HttpRequest request, ClientHttpResponse response) throws IOException {
        var statusCode = response.getStatusCode();

        if (!statusCode.isSameCodeAs(HttpStatus.FORBIDDEN)
                && !statusCode.isSameCodeAs(HttpStatus.UNAUTHORIZED)) {
            return;
        }

        throw new GithubRequestWasUnauthenticatedException(
                MessageFormat.format(
                        "Request to {0} returned a {1} status code.",
                        request.getURI(),
                        response.getStatusCode()
                )
        );
    }
}
