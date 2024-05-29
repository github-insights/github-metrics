package be.xplore.githubmetrics.githubadapter.mappingclasses;

import be.xplore.githubmetrics.domain.selfhostedrunner.OperatingSystem;
import be.xplore.githubmetrics.domain.selfhostedrunner.SelfHostedRunnerStatus;
import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GHSelfHostedRunnerTest {
    private static final String LINUX = "linux";
    private static final String ONLINE = "online";
    private static final String OFFLINE = "offline";
    private GithubProperties.Parsing.SelfHostedRunnerOsKeywords keywords;

    @BeforeEach
    void setUp() {
        this.keywords = new GithubProperties.Parsing.SelfHostedRunnerOsKeywords(
                "", "", "vmapple"
        );
    }

    @Test
    void onlineAndBusyShouldLeadToBusyRunner() {
        var ghRunner = new GHSelfHostedRunner(LINUX, ONLINE, true);
        assertEquals(
                SelfHostedRunnerStatus.BUSY,
                ghRunner.getRunner(this.keywords).getSelfHostedRunnerStatus()
        );
    }

    @Test
    void onlineAndNotBusyShouldLeadToIdleRunner() {
        var ghRunner = new GHSelfHostedRunner(LINUX, ONLINE, false);
        assertEquals(
                SelfHostedRunnerStatus.IDLE,
                ghRunner.getRunner(this.keywords).getSelfHostedRunnerStatus()
        );
    }

    @Test
    void offlineShouldAlwaysLeadToOfflineState() {
        var ghRunner1 = new GHSelfHostedRunner(LINUX, OFFLINE, false);
        var ghRunner2 = new GHSelfHostedRunner(LINUX, OFFLINE, true);
        assertEquals(
                SelfHostedRunnerStatus.OFFLINE,
                ghRunner1.getRunner(this.keywords).getSelfHostedRunnerStatus()
        );
        assertEquals(
                SelfHostedRunnerStatus.OFFLINE,
                ghRunner2.getRunner(this.keywords).getSelfHostedRunnerStatus()
        );
    }

    @Test
    void unknownOperatingSystemNameShouldThrowException() {
        var ghRunner1 = new GHSelfHostedRunner(
                "unkown operating system", OFFLINE, false
        );
        assertEquals(
                OperatingSystem.UNKNOWN,
                ghRunner1.getRunner(this.keywords).getOperatingSystem()
        );
    }

    @Test
    void unknownStatusShouldThrowException() {
        var ghRunner1 = new GHSelfHostedRunner(
                LINUX, "unknown status", false
        );
        assertThrows(
                IllegalStateException.class,
                () -> ghRunner1.getRunner(this.keywords)
        );
    }
}