package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.repository.RepositoriesQueryPort;
import be.xplore.githubmetrics.domain.repository.Repository;
import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import be.xplore.githubmetrics.githubadapter.mappingclasses.GHRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
    private final GithubProperties config;
    private final GithubAdapter githubAdapter;

    public RepositoriesAdapter(GithubProperties config, GithubAdapter githubAdapter) {
        this.config = config;
        this.githubAdapter = githubAdapter;
    }

    @Cacheable("Repositories")
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
        ResponseEntity<GHRepository[]> responseEntity = GithubAdapter.getEntity(
                this.githubAdapter
                        .getResponseSpec(path, parameters),
                GHRepository[].class
        );

        return conditionallyFetchNextPage(responseEntity);
    }

    private List<Repository> fetchRepositories(String fullUrl) {
        ResponseEntity<GHRepository[]> responseEntity = GithubAdapter.getEntity(
                this.githubAdapter
                        .getResponseSpec(fullUrl),
                GHRepository[].class
        );

        return conditionallyFetchNextPage(responseEntity);
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
