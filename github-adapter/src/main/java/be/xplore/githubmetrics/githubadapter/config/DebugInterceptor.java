package be.xplore.githubmetrics.githubadapter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class DebugInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DebugInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        LOGGER.trace("The body of the request {} bytes long.", body.length);
        var response = execution.execute(request, body);
        LOGGER.trace(
                "{} request to {} returned status code {}",
                request.getMethod(),
                request.getURI(),
                response.getStatusCode()
        );
        LOGGER.trace(
                "Headers of response were {}.",
                response.getHeaders()
        );

        Optional.ofNullable(response.getHeaders().get("x-ratelimit-remaining")).flatMap(header -> Optional.ofNullable(header.getFirst())).ifPresent(rateRemaining -> {
            var intRateRemaining = Integer.parseInt(rateRemaining);
            if (intRateRemaining < 500 && intRateRemaining % 50 == 0) {
                LOGGER.error("Current rate remaining is {}", intRateRemaining);
            }
            if (intRateRemaining % 500 == 0) {
                LOGGER.info("Current rate remaining is {}.", intRateRemaining);
            } else if (intRateRemaining % 100 == 0) {
                LOGGER.debug("Current rate remaining is {}.", intRateRemaining);
            }

        });
        return response;
    }
}