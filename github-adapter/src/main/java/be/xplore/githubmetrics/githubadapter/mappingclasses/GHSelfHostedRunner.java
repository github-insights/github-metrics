package be.xplore.githubmetrics.githubadapter.mappingclasses;

import be.xplore.githubmetrics.domain.selfhostedrunner.OperatingSystem;
import be.xplore.githubmetrics.domain.selfhostedrunner.SelfHostedRunner;
import be.xplore.githubmetrics.domain.selfhostedrunner.SelfHostedRunnerStatus;

import java.text.MessageFormat;

public record GHSelfHostedRunner(
        String os,
        String status,
        boolean busy
) {
    public SelfHostedRunner getRunner() {
        return new SelfHostedRunner(this.getOs(), this.getStatus());
    }

    private OperatingSystem getOs() {
        return switch (this.os) {
            case "linux" -> OperatingSystem.LINUX;
            case "windows" -> OperatingSystem.WINDOWS;
            case "macOS" -> OperatingSystem.MAC_OS;
            default -> throw new IllegalStateException(MessageFormat.format(
                    "The string {0} could not be parsed to the OperatingSystem enum.",
                    this.os
            ));
        };
    }

    private SelfHostedRunnerStatus getStatus() {
        return switch (this.status) {
            case "offline" -> SelfHostedRunnerStatus.OFFLINE;
            case "online" -> {
                if (this.busy) {
                    yield SelfHostedRunnerStatus.BUSY;
                } else {
                    yield SelfHostedRunnerStatus.IDLE;
                }
            }
            default -> throw new IllegalStateException(MessageFormat.format(
                    "The status {0} is not one of (online, offline), meaning it cant be parsed to a SelfHostedRunnerStatus.",
                    this.status
            ));
        };
    }
}
