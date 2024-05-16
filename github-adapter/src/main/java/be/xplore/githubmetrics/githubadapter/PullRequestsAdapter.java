package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.pullrequest.PullRequest;
import be.xplore.githubmetrics.domain.pullrequest.PullRequestQueryPort;
import be.xplore.githubmetrics.domain.repository.Repository;
import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import be.xplore.githubmetrics.githubadapter.mappingclasses.GHPullRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Service
public class PullRequestsAdapter implements PullRequestQueryPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(PullRequestsAdapter.class);
    private final GithubProperties githubProperties;
    private final RestClient restClient;
    private final GithubApiUtilities utilities;

    public PullRequestsAdapter(
            GithubProperties githubProperties,
            @Qualifier("defaultRestClient") RestClient restClient,
            GithubApiUtilities utilities
    ) {
        this.githubProperties = githubProperties;
        this.restClient = restClient;
        this.utilities = utilities;
    }

    private String getPullRequestsApiPath(String repoName) {
        return MessageFormat.format(
                "/repos/{0}/{1}/pulls",
                this.githubProperties.org(),
                repoName
        );
    }

    @Override
    public List<PullRequest> getAllPullRequestsForRepository(Repository repository) {
        LOGGER.info("Fetching fresh PullRequests for Repository {}", repository.getId());
        var parameters = new HashMap<String, String>();
        parameters.put("state", "all");
        parameters.put("per_page", "100");

        ResponseEntity<GHPullRequest[]> responseEntity = this.restClient.get()
                .uri(utilities.setPathAndParameters(
                        this.getPullRequestsApiPath(repository.getName()),
                        parameters
                ))
                .retrieve()
                .toEntity(GHPullRequest[].class);

        var pullRequests = this.utilities.followPaginationLink(
                responseEntity,
                ghPullRequests -> Arrays.stream(ghPullRequests).map(GHPullRequest::getPullRequest).toList(),
                GHPullRequest[].class
        );

        LOGGER.debug(
                "Response for the PullRequests fetch of Repository {} returned {} PullRequests.",
                repository.getId(),
                pullRequests.size()
        );

        return pullRequests;
    }
}
