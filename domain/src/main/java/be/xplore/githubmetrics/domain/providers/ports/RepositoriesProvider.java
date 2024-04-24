package be.xplore.githubmetrics.domain.providers.ports;

import be.xplore.githubmetrics.domain.domain.Repository;

import java.util.List;

public interface RepositoriesProvider {
    List<Repository> getAllRepositories();
}
