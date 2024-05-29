package be.xplore.githubmetrics.githubadapter.mappingclasses;

import be.xplore.githubmetrics.domain.selfhostedrunner.SelfHostedRunner;

import java.util.List;

public record GHSelfHostedRunners(
        long total_count,
        List<GHSelfHostedRunner> runners
) {

    public static final String PATH_ORG = "orgs/{org}/actions/runners";
    public static final String PATH_REPO = "repos/{org}/{repo}/actions/runners";

    public List<SelfHostedRunner> getRunners() {
        return this.runners.stream().map(GHSelfHostedRunner::getRunner).toList();
    }
}
