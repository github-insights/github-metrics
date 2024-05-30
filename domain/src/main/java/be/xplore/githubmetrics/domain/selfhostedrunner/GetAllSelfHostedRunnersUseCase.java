package be.xplore.githubmetrics.domain.selfhostedrunner;

import be.xplore.githubmetrics.domain.repository.RepositoriesQueryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetAllSelfHostedRunnersUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetAllSelfHostedRunnersUseCase.class);

    private final RepositoriesQueryPort repositoriesQuery;
    private final SelfHostedRunnersQueryPort selfHostedRunnersQueryPort;

    public GetAllSelfHostedRunnersUseCase(
            RepositoriesQueryPort repositoriesQuery,
            SelfHostedRunnersQueryPort selfHostedRunnersQueryPort
    ) {
        this.repositoriesQuery = repositoriesQuery;
        this.selfHostedRunnersQueryPort = selfHostedRunnersQueryPort;
    }

    public List<SelfHostedRunner> getAllSelfHostedRunners() {
        var repositories = this.repositoriesQuery.getAllRepositories();
        LOGGER.debug(
                "Exporting self hosted runners for organization and {} repositories",
                repositories.size()
        );
        return this.selfHostedRunnersQueryPort.getAllSelfHostedRunners(repositories);
    }
}
