package be.xplore.githubmetrics.domain.selfhostedrunner;

import be.xplore.githubmetrics.domain.repository.RepositoriesQueryPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetAllSelfHostedRunnersUseCase {
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
        return this.selfHostedRunnersQueryPort.getAllSelfHostedRunners(repositories);
    }
}
