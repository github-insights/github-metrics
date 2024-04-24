package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.githubadapter.config.GithubConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class GithubAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(GithubAdapter.class);
    private final RestClient restClient;
    private final GithubConfig config;

    public GithubAdapter(GithubConfig config) {
        this.config = config;
        this.restClient = RestClient.builder()
                .defaultHeaders(
                        httpHeaders -> {
                            httpHeaders.set("X-Github-Api-Version", "2022-11-28");
                            httpHeaders.set("Authorization", this.config.token());
                        })
                .build();
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

