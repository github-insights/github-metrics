package be.xplore.githubmetrics.domain.queries;

import be.xplore.githubmetrics.domain.domain.Repository;

import java.util.List;

public interface RepositoriesQueryPort {

    List<Repository> getAllRepositories();
}
