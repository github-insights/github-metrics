package be.xplore.githubmetrics.githubadapter.mappingclasses;

import be.xplore.githubmetrics.domain.selfhostedrunner.SelfHostedRunner;

import java.util.List;

public record GHSelfHostedRunners(
        long total_count,
        List<GHSelfHostedRunner> runners
) {
    public List<SelfHostedRunner> getRunners() {
        return this.runners.stream().map(GHSelfHostedRunner::getRunner).toList();
    }
}
