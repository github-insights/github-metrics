package be.xplore.githubmetrics.domain.usecases;

import be.xplore.githubmetrics.domain.domain.Repository;

import java.util.List;

public interface GetAllRepositoriesUseCase {
    List<Repository> getAllRepositories();
}
