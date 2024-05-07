package be.xplore.githubmetrics.domain.repository;

import java.util.List;

public interface RepositoriesQueryPort {

    List<Repository> getAllRepositories();
}
