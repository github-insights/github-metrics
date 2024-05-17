package be.xplore.githubmetrics.githubadapter.mappingclasses;

import be.xplore.githubmetrics.domain.selfhostedrunner.SelfHostedRunnerStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GHSelfHostedRunnerTest {
    private static final String LINUX = "linux";
    private static final String ONLINE = "online";
    private static final String OFFLINE = "offline";

    @Test
    void onlineAndBusyShouldLeadToBusyRunner() {
        var ghRunner = new GHSelfHostedRunner(LINUX, ONLINE, true);
        assertEquals(
                SelfHostedRunnerStatus.BUSY,
                ghRunner.getRunner().getSelfHostedRunnerStatus()
        );
    }

    @Test
    void onlineAndNotBusyShouldLeadToIdleRunner() {
        var ghRunner = new GHSelfHostedRunner(LINUX, ONLINE, false);
        assertEquals(
                SelfHostedRunnerStatus.IDLE,
                ghRunner.getRunner().getSelfHostedRunnerStatus()
        );
    }

    @Test
    void offlineShouldAlwaysLeadToOfflineState() {
        var ghRunner1 = new GHSelfHostedRunner(LINUX, OFFLINE, false);
        var ghRunner2 = new GHSelfHostedRunner(LINUX, OFFLINE, true);
        assertEquals(
                SelfHostedRunnerStatus.OFFLINE,
                ghRunner1.getRunner().getSelfHostedRunnerStatus()
        );
        assertEquals(
                SelfHostedRunnerStatus.OFFLINE,
                ghRunner2.getRunner().getSelfHostedRunnerStatus()
        );
    }

    @Test
    void unknownOperatingSystemNameShouldThrowException() {
        var ghRunner1 = new GHSelfHostedRunner(
                "unkown operating system", OFFLINE, false
        );
        assertThrows(
                IllegalStateException.class,
                ghRunner1::getRunner
        );
    }

    @Test
    void unknownStatusShouldThrowException() {
        var ghRunner1 = new GHSelfHostedRunner(
                LINUX, "unknown status", false
        );
        assertThrows(
                IllegalStateException.class,
                ghRunner1::getRunner
        );
    }
}