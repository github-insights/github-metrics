package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.repository.Repository;
import be.xplore.githubmetrics.domain.selfhostedrunner.SelfHostedRunner;
import be.xplore.githubmetrics.domain.selfhostedrunner.SelfHostedRunnersQueryPort;
import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import be.xplore.githubmetrics.githubadapter.mappingclasses.GHSelfHostedRunners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

@Service
public class SelfHostedRunnerAdapter implements SelfHostedRunnersQueryPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelfHostedRunnerAdapter.class);
    private final GithubProperties githubProperties;
    private final RestClient restClient;
    private final GithubApiUtilities utilities;

    public SelfHostedRunnerAdapter(
            GithubProperties githubProperties,
            @Qualifier("defaultRestClient") RestClient restClient,
            GithubApiUtilities utilities) {
        this.githubProperties = githubProperties;
        this.restClient = restClient;
        this.utilities = utilities;
    }

    @Override
    public List<SelfHostedRunner> getAllSelfHostedRunners(List<Repository> repositories) {
        var organizationRunners = this.getForOrganization();
        var repositoryRunners = this.getForRepositories(repositories);
        return Stream.concat(organizationRunners.stream(), repositoryRunners.stream()).toList();
    }

    private List<SelfHostedRunner> getForOrganization() {
        LOGGER.info("Fetching fresh SelfHostedRunners for Organization.");
        var selfHostedRunners = this.fetchSelfHostedRunners(MessageFormat.format(
                "orgs/{0}/actions/runners",
                this.githubProperties.org()
        ));
        LOGGER.debug(
                "Response for the SelfHostedRunner fetch of Organization returned {} SelfHostedRunners.",
                selfHostedRunners.size()
        );

        return selfHostedRunners;
    }

    private List<SelfHostedRunner> getForRepositories(List<Repository> repositories) {
        return repositories.stream().map(this::getForRepository).flatMap(List::stream).toList();
    }

    private List<SelfHostedRunner> getForRepository(Repository repository) {
        LOGGER.info("Fetching fresh SelfHostedRunners for Repository {}.", repository.getName());
        var selfHostedRunners = this.fetchSelfHostedRunners(MessageFormat.format(
                "repos/{0}/{1}/actions/runners",
                this.githubProperties.org(),
                repository.getName()
        ));
        LOGGER.debug(
                "Response for the SelfHostedRunner fetch of Repository {} returned {} SelfHostedRunners.",
                repository.getName(),
                selfHostedRunners.size()
        );

        return selfHostedRunners;
    }

    private List<SelfHostedRunner> fetchSelfHostedRunners(
            String link
    ) {
        var parameters = new HashMap<String, String>();
        parameters.put("per_page", "100");
        ResponseEntity<GHSelfHostedRunners> responseEntity = this.restClient.get()
                .uri(utilities.setPathAndParameters(
                        link, parameters
                ))
                .retrieve()
                .toEntity(GHSelfHostedRunners.class);

        return this.utilities.followPaginationLink(
                responseEntity,
                GHSelfHostedRunners::getRunners,
                GHSelfHostedRunners.class
        );
    }
}
