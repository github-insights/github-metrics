package be.xplore.githubmetrics.prometheusexporter.selfhostedrunner;

import be.xplore.githubmetrics.domain.selfhostedrunner.OperatingSystem;
import be.xplore.githubmetrics.domain.selfhostedrunner.SelfHostedRunnerStatus;

public record SelfHostedRunnerState(
        OperatingSystem operatingSystem,
        SelfHostedRunnerStatus status
) {
}
