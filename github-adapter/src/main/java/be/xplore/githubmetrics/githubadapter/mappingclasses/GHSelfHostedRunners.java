package be.xplore.githubmetrics.githubadapter.mappingclasses;

import be.xplore.githubmetrics.domain.selfhostedrunner.SelfHostedRunner;
import be.xplore.githubmetrics.githubadapter.config.GithubProperties;

import java.util.List;

public record GHSelfHostedRunners(
        long total_count,
        List<GHSelfHostedRunner> runners
) {

    public List<SelfHostedRunner> getRunners(GithubProperties.Parsing.SelfHostedRunnerOsKeywords keywords) {
        return this.runners.stream().map(ghSelfHostedRunner -> ghSelfHostedRunner.getRunner(keywords)).toList();
    }
}
