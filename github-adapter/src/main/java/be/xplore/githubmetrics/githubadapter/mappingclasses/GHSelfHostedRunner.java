package be.xplore.githubmetrics.githubadapter.mappingclasses;

import be.xplore.githubmetrics.domain.selfhostedrunner.OperatingSystem;
import be.xplore.githubmetrics.domain.selfhostedrunner.SelfHostedRunner;
import be.xplore.githubmetrics.domain.selfhostedrunner.SelfHostedRunnerStatus;
import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;

public record GHSelfHostedRunner(
        String os,
        String status,
        boolean busy
) {
    private static final Logger LOGGER = LoggerFactory.getLogger(GHSelfHostedRunner.class);

    public SelfHostedRunner getRunner(GithubProperties.Parsing.SelfHostedRunnerOsKeywords keywords) {
        return new SelfHostedRunner(this.getOs(keywords), this.getStatus());
    }

    @SuppressWarnings("checkstyle:ReturnCount")
    private OperatingSystem getOs(GithubProperties.Parsing.SelfHostedRunnerOsKeywords keywords) {
        if (osContainsAny(keywords.linuxKeywords())) {
            return OperatingSystem.LINUX;
        } else if (osContainsAny(keywords.windowsKeywords())) {
            return OperatingSystem.WINDOWS;
        } else if (osContainsAny(keywords.macosKeywords())) {
            return OperatingSystem.MAC_OS;
        } else {
            LOGGER.warn("Unknown operating system encountered. Please update the keywords so that we are able to match the os to one of the options.");
            LOGGER.warn("Consider that all keywords and os's are treated as lowercase. Os was {}", this.os);
            return OperatingSystem.UNKNOWN;
        }
    }

    private boolean osContainsAny(List<String> list) {
        return list.stream().anyMatch(this.os.toLowerCase(Locale.ROOT)::contains);
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
