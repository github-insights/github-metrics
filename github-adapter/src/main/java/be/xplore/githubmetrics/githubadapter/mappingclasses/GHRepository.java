package be.xplore.githubmetrics.githubadapter.mappingclasses;

import be.xplore.githubmetrics.domain.repository.Repository;

import java.util.List;

public record GHRepository(
        long id,
        String name,
        String full_name,
        List<String> topics
) {

    public Repository getRepository() {
        return new Repository(this.id, this.name, this.full_name, this.topics);
    }
}
