package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.githubadapter.config.GithubConfig;
import be.xplore.githubmetrics.githubadapter.exceptions.UnableToParseGithubResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.text.MessageFormat;
import java.util.Map;

@Component
public class GithubAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(GithubAdapter.class);
    private final RestClient restClient;
    private final GithubConfig config;

    public GithubAdapter(GithubConfig config, RestClient restClient) {
        this.config = config;
        this.restClient = restClient;
    }

    protected static <T> ResponseEntity<T> getEntity(
            RestClient.ResponseSpec response,
            Class<T> clazz
    ) {
        try {
            return response.toEntity(clazz);
        } catch (RestClientException e) {
            throw new UnableToParseGithubResponseException(
                    MessageFormat.format(
                            "Could not parse Githubs response into {0}!",
                            clazz
                    ),
                    e
            );
        }
    }

    protected static <T> T getBody(
            RestClient.ResponseSpec response,
            Class<T> clazz
    ) {
        var body = GithubAdapter.getEntity(response, clazz).getBody();
        if (body == null) {
            throw new UnableToParseGithubResponseException(
                    MessageFormat.format(
                            "Could not get Body out of response trying to parse to {0}!",
                            clazz
                    )
            );
        }
        return body;
    }

    public RestClient.ResponseSpec getResponseSpec(String path, Map<String, String> queryParams) {
        LOGGER.info("Doing path request for {} path and with {} parameters", path, queryParams.size());
        return restClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .scheme(config.schema())
                            .host(config.host())
                            .port(config.port())
                            .path(path);
                    for (final var queryParam : queryParams.entrySet()) {
                        builder = builder.queryParam(queryParam.getKey(), queryParam.getValue());
                    }
                    LOGGER.debug(builder.toUriString());
                    return builder.build();

                }).retrieve();
    }

    public RestClient.ResponseSpec getResponseSpec(String fullUrl) {
        LOGGER.info("Doing full url request for url: {}", fullUrl);
        return restClient.get()
                .uri(fullUrl)
                .retrieve();
    }

    public GithubConfig getConfig() {
        return config;
    }
}

