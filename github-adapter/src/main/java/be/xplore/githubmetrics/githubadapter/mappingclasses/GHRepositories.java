package be.xplore.githubmetrics.githubadapter.mappingclasses;

import be.xplore.githubmetrics.domain.repository.Repository;

import java.util.ArrayList;
import java.util.List;

public record GHRepositories(
        List<GHRepository> repositories
) {
    public List<Repository> getRepositories() {
        List<Repository> repositoryList = new ArrayList<>();
        repositories.forEach(repository -> repositoryList.add(repository.getRepository()));
        return repositoryList;
    }
}
