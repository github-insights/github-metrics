package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.repository.RepositoriesQueryPort;
import be.xplore.githubmetrics.domain.repository.Repository;
import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import be.xplore.githubmetrics.githubadapter.mappingclasses.GHRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class RepositoriesAdapter implements RepositoriesQueryPort {
    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoriesAdapter.class);
    private final RestClient restClient;
    private final GithubProperties config;

    public RepositoriesAdapter(
            GithubProperties config,
            @Qualifier("defaultRestClient") RestClient restClient
    ) {
        this.restClient = restClient;
        this.config = config;
    }

    @Cacheable("Repositories")
    @Override
    public List<Repository> getAllRepositories() {
        var parameters = new HashMap<String, String>();
        parameters.put("per_page", "100");

        var repositories = this.fetchRepositories(
                MessageFormat.format(
                        "/orgs/{0}/repos",
                        this.config.org()
                ),
                parameters
        );
        LOGGER.info("Correctly fetched {} repositories.", repositories.size());
        return repositories;
    }

    private List<Repository> fetchRepositories(String path, Map<String, String> parameters) {
        ResponseEntity<GHRepository[]> response = this.restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(path);
                    for (final var parameter : parameters.entrySet()) {
                        uriBuilder.queryParam(parameter.getKey(), parameter.getValue());
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .toEntity(GHRepository[].class);

        return conditionallyFetchNextPage(response);
    }

    private List<Repository> fetchRepositories(String fullUrl) {
        ResponseEntity<GHRepository[]> response = this.restClient.get()
                .uri(fullUrl)
                .retrieve()
                .toEntity(GHRepository[].class);

        return conditionallyFetchNextPage(response);
    }

    private List<Repository> conditionallyFetchNextPage(
            ResponseEntity<GHRepository[]> previousResponse
    ) {
        List<Repository> repositories = new ArrayList<>(
                this.getRepositoriesFromResponse(previousResponse)
        );

        if (previousResponse.getHeaders().containsKey("link")) {
            var linkHeader = previousResponse.getHeaders()
                    .getValuesAsList("link")
                    .getFirst();
            getNextPageLinkFromHeader(linkHeader).ifPresent(nextPageUrl ->
                    repositories.addAll(this.fetchRepositories(nextPageUrl))
            );
        }

        return repositories;
    }

    private List<Repository> getRepositoriesFromResponse(ResponseEntity<GHRepository[]> response) {
        GHRepository[] repositories = response.getBody();
        return Stream.of(repositories)
                .map(GHRepository::getRepository)
                .toList();
    }

    private Optional<String> getNextPageLinkFromHeader(String linkHeader) {
        var optNextSection = Arrays.stream(linkHeader.split(","))
                .filter(part -> part.contains("rel=\"next\""))
                .findFirst();

        if (optNextSection.isEmpty()) {
            return optNextSection;
        }
        var nextSection = optNextSection.get();
        return Arrays.stream(nextSection.split(";"))
                .findFirst()
                .map(bracedLink ->
                        bracedLink.substring(1, bracedLink.length() - 1)
                );
    }

}
