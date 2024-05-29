package be.xplore.githubmetrics.githubadapter.config;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.observation.ClientHttpObservationDocumentation;
import org.springframework.http.client.observation.ClientRequestObservationContext;
import org.springframework.http.client.observation.ClientRequestObservationConvention;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class GithubRestClientRequestObservationConvention implements ClientRequestObservationConvention {
    private static final Logger LOGGER = LoggerFactory.getLogger(GithubRestClientRequestObservationConvention.class);

    @Override
    public String getName() {
        return "http.client.requests";
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(ClientRequestObservationContext context) {
        return KeyValues.of(method(context), status(context), uri(context));
    }

    protected KeyValue uri(ClientRequestObservationContext context) {
        String uri = Optional.ofNullable(context.getCarrier().getHeaders().get("path")).map(list -> {
            if (!list.isEmpty()) {
                return list.getFirst();
            } else {
                return "NO HEADER";
            }
        }).orElse("NO HEADER");
        return KeyValue.of("uri", uri);
    }

    protected KeyValue method(ClientRequestObservationContext context) {
        return KeyValue.of(ClientHttpObservationDocumentation.LowCardinalityKeyNames.METHOD, context.getCarrier().getMethod().toString());
    }

    protected KeyValue status(ClientRequestObservationContext context) {
        String statusCode = Optional.ofNullable(context.getResponse()).map(response -> {
            String out = "";
            try {
                HttpStatusCode httpStatusCode = response.getStatusCode();
                if (httpStatusCode.is1xxInformational()) {
                    out = "1xx";
                } else if (httpStatusCode.is2xxSuccessful()) {
                    out = "2xx";
                } else if (httpStatusCode.is3xxRedirection()) {
                    out = "3xx";
                } else if (httpStatusCode.is4xxClientError()) {
                    out = "4xx";
                } else if (httpStatusCode.is5xxServerError()) {
                    out = "5xx";
                }
            } catch (IOException e) {
                LOGGER.trace("ERROR: Status Code was not preset on the context response.");
            }
            return out;
        }).orElse("");

        return KeyValue.of(ClientHttpObservationDocumentation.LowCardinalityKeyNames.STATUS, statusCode);
    }

}
