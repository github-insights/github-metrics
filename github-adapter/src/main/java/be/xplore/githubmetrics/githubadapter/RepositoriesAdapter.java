package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.domain.Repository;
import be.xplore.githubmetrics.domain.usecases.ports.in.RepositoriesQueryPort;
import be.xplore.githubmetrics.githubadapter.config.GithubConfig;
import be.xplore.githubmetrics.githubadapter.exceptions.UnableToParseGHRepositoryArrayException;
import be.xplore.githubmetrics.githubadapter.mappingclasses.GHRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

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
    private final GithubConfig config;
    private final GithubAdapter githubAdapter;

    public RepositoriesAdapter(GithubConfig config, GithubAdapter githubAdapter) {
        this.config = config;
        this.githubAdapter = githubAdapter;
    }

    @Override
    public List<Repository> getAllRepositories() {
        var repositories = this.fetchRepositories(
                MessageFormat.format(
                        "/orgs/{0}/repos",
                        this.config.org()
                ),
                new HashMap<>()
        );
        LOGGER.info("Correctly fetched {} repositories.", repositories.size());
        return repositories;
    }

    private List<Repository> fetchRepositories(String path, Map<String, String> parameters) {
        ResponseEntity<GHRepository[]> responseEntity = this.getEntity(
                this.githubAdapter
                        .getResponseSpec(path, parameters)
        );

        return conditionallyFetchNextPage(responseEntity);
    }

    private List<Repository> fetchRepositories(String fullUrl) {
        ResponseEntity<GHRepository[]> responseEntity = this.getEntity(
                this.githubAdapter
                        .getResponseSpec(fullUrl)
        );

        return conditionallyFetchNextPage(responseEntity);
    }

    private ResponseEntity<GHRepository[]> getEntity(
            RestClient.ResponseSpec response
    ) {
        try {
            return response.toEntity(GHRepository[].class);
        } catch (RestClientException e) {
            throw new UnableToParseGHRepositoryArrayException(e.getMessage());
        }
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
