package be.xplore.githubmetrics.domain.repository;

import java.util.List;

public class Repository {
    private final long id;
    private final String name;
    private final String fullName;
    private final List<String> topics;

    public Repository(long id, String name, String fullName, List<String> topics) {
        this.id = id;
        this.name = name;
        this.fullName = fullName;
        this.topics = topics;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }

    public List<String> getTopics() {
        return topics;
    }
}
