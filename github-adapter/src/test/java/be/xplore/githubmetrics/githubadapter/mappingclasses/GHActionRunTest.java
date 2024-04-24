package be.xplore.githubmetrics.githubadapter.mappingclasses;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GHActionRunTest {
    private final List<String> allStatuses = List.of("completed", "success",
            "action_required", "cancelled", "failure", "neutral", "skipped",
            "stale", "timed_out", "in_progress", "queued", "requested",
            "waiting", "pending"
    );

    @Test
    void convertingStatusCanHandleAllPossibleGithubStatuses() {
        assertDoesNotThrow(
                () -> allStatuses.forEach(status ->
                        new GHActionRun(0, "", status, 0, "", "")
                                .convertStatus()
                )
        );
    }

    @Test
    void convertingStatusThrowsIllegalStateExceptionIfStringUnknownStatus() {
        var actionRun = new GHActionRun(
                0, "",
                "radom unknown status that definetly does not exist",
                0, "", ""
        );
        assertThrows(
                IllegalStateException.class,
                () -> actionRun.convertStatus()
        );
    }
}

