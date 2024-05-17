package be.xplore.githubmetrics.domain.repository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetAllRepositoriesUseCase {
    private final RepositoriesQueryPort repositoriesQueryPort;

    public GetAllRepositoriesUseCase(RepositoriesQueryPort repositoriesQueryPort) {
        this.repositoriesQueryPort = repositoriesQueryPort;
    }

    public List<Repository> getAllRepositories() {
        return this.repositoriesQueryPort.getAllRepositories();
    }
}
