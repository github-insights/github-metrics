package be.xplore.githubmetrics.domain.selfhostedrunner;

import be.xplore.githubmetrics.domain.repository.Repository;

import java.util.List;

public interface SelfHostedRunnersQueryPort {
    List<SelfHostedRunner> getAllSelfHostedRunners(List<Repository> repositories);
}
