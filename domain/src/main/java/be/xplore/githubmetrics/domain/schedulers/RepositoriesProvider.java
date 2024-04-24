package be.xplore.githubmetrics.domain.schedulers;

import be.xplore.githubmetrics.domain.domain.Repository;
import be.xplore.githubmetrics.domain.usecases.GetAllRepositoriesUseCase;
import be.xplore.githubmetrics.domain.usecases.ports.in.RepositoriesQueryPort;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RepositoriesProvider implements GetAllRepositoriesUseCase {

    private final RepositoriesQueryPort repositoriesQueryPort;

    public RepositoriesProvider(RepositoriesQueryPort repositoriesQueryPort) {
        this.repositoriesQueryPort = repositoriesQueryPort;
    }

    @Cacheable("repositories")
    @Override
    public List<Repository> getAllRepositories() {
        return this.repositoriesQueryPort.getAllRepositories();
    }
}
