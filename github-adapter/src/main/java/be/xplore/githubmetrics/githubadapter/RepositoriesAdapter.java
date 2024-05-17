package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.repository.RepositoriesQueryPort;
import be.xplore.githubmetrics.domain.repository.Repository;
import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import be.xplore.githubmetrics.githubadapter.mappingclasses.GHRepositories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;

@Service
public class RepositoriesAdapter implements RepositoriesQueryPort {
    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoriesAdapter.class);
    private final RestClient restClient;
    private final GithubProperties config;
    private final GithubApiUtilities utilities;

    public RepositoriesAdapter(
            GithubProperties config,
            @Qualifier("defaultRestClient") RestClient restClient, GithubApiUtilities utilities
    ) {
        this.restClient = restClient;
        this.config = config;
        this.utilities = utilities;
    }

    @Cacheable("Repositories")
    @Override
    public List<Repository> getAllRepositories() {
        LOGGER.info("Fetching fresh Repositories.");

        var parameters = new HashMap<String, String>();
        parameters.put("per_page", "100");

        ResponseEntity<GHRepositories> responseEntity = this.restClient.get()
                .uri(utilities.setPathAndParameters(
                        this.getRepositoriesApiPath(),
                        parameters
                ))
                .retrieve()
                .toEntity(GHRepositories.class);

        var repositories = this.utilities.followPaginationLink(
                responseEntity,
                GHRepositories::getRepositories,
                GHRepositories.class
        );

        LOGGER.debug(
                "Response for the Repositories fetch returned {} repositories",
                repositories.size()
        );
        return repositories;
    }

    private String getRepositoriesApiPath() {
        return "/installation/repositories";
    }

}
