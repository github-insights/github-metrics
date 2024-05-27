package be.xplore.githubmetrics.domain.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetAllRepositoriesUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetAllRepositoriesUseCase.class);

    private final RepositoriesQueryPort repositoriesQueryPort;

    public GetAllRepositoriesUseCase(RepositoriesQueryPort repositoriesQueryPort) {
        this.repositoriesQueryPort = repositoriesQueryPort;
    }

    public List<Repository> getAllRepositories() {
        LOGGER.info("Exporting all available Repositories.");
        return this.repositoriesQueryPort.getAllRepositories();
    }
}
