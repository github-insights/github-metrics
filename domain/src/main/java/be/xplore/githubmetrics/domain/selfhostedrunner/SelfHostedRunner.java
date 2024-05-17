package be.xplore.githubmetrics.domain.selfhostedrunner;

public class SelfHostedRunner {
    private final OperatingSystem operatingSystem;
    private final SelfHostedRunnerStatus selfHostedRunnerStatus;

    public SelfHostedRunner(OperatingSystem operatingSystem, SelfHostedRunnerStatus selfHostedRunnerStatus) {
        this.operatingSystem = operatingSystem;
        this.selfHostedRunnerStatus = selfHostedRunnerStatus;
    }

    public OperatingSystem getOperatingSystem() {
        return operatingSystem;
    }

    public SelfHostedRunnerStatus getSelfHostedRunnerStatus() {
        return selfHostedRunnerStatus;
    }
}
