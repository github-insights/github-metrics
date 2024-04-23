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
        restClient = RestClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeaders(
                        httpHeaders -> {
                            httpHeaders.set("X-Github-Api-Version", "2022-11-28");
                            httpHeaders.set("Authorization", this.config.token());
                        })
                .build();
    }

    public RestClient.ResponseSpec getResponseSpec(String uri, Map<String, String> queryParams) {
        LOGGER.info("Getting Workflow Run Response");

        return restClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path(uri);
                    for (final var queryParam : queryParams.entrySet()) {
                        builder = builder.queryParam(queryParam.getKey(), queryParam.getValue());
                    }
                    return builder.build();
                })
                .retrieve();
    }
}
