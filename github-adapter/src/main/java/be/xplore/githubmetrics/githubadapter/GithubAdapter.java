package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.githubadapter.config.GithubConfig;
import be.xplore.githubmetrics.githubadapter.exceptions.InvalidAdapterRequestURIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Map;

@Component
public class GithubAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(GithubAdapter.class);
    private final RestClient restClient;
    private final GithubConfig config;

    public GithubAdapter(GithubConfig config) {
        this.config = config;
        restClient = RestClient.builder()
                .defaultHeaders(
                        httpHeaders -> {
                            httpHeaders.set("X-Github-Api-Version", "2022-11-28");
                            httpHeaders.set("Authorization", this.config.token());
                        })
                .build();
    }

    public RestClient.ResponseSpec getResponseSpec(
            String stringUri,
            Map<String, String> queryParams
    ) {
        LOGGER.info("Getting Workflow Run Response");

        return restClient.get()
                .uri(uriBuilder -> {
                    URI uri = null;
                    try {
                        uri = new URI(stringUri);
                    } catch (URISyntaxException e) {
                        throw new InvalidAdapterRequestURIException(
                                MessageFormat.format(
                                        "This {0} uri is formatted incorrectly",
                                        stringUri
                                )
                        );
                    }
                    var builder = uriBuilder.host(uri.getHost())
                            .scheme(uri.getScheme())
                            .port(uri.getPort())
                            .path(uri.getPath());
                    for (final var queryParam : queryParams.entrySet()) {
                        builder = builder.queryParam(queryParam.getKey(), queryParam.getValue());
                    }
                    LOGGER.debug(builder.toUriString());
                    return builder.build();

                }).retrieve();
    }
}
