package be.xplore.githubmetrics.domain.providers;

import be.xplore.githubmetrics.domain.domain.Repository;
import be.xplore.githubmetrics.domain.providers.ports.RepositoriesProvider;
import be.xplore.githubmetrics.domain.queries.RepositoriesQueryPort;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CachedRepositoriesProvider implements RepositoriesProvider {

    private final RepositoriesQueryPort repositoriesQueryPort;

    public CachedRepositoriesProvider(RepositoriesQueryPort repositoriesQueryPort) {
        this.repositoriesQueryPort = repositoriesQueryPort;
    }

    @Cacheable("repositories.all")
    @Override
    public List<Repository> getAllRepositories() {
        return this.repositoriesQueryPort.getAllRepositories();
    }
}
