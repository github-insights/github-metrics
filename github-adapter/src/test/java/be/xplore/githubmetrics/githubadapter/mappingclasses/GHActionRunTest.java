package be.xplore.githubmetrics.githubadapter.mappingclasses;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GHActionRunTest {
    private final List<String> allStatuses = List.of("completed", "success",
            "action_required", "cancelled", "failure", "neutral", "skipped",
            "stale", "timed_out", "in_progress", "queued", "requested",
            "waiting", "pending"
    );

    private final List<String> allConclusions = List.of(
            "failure", "success", "neutral", "in_progress"
    );

    @Test
    void convertingStatusCanHandleAllPossibleGithubStatuses() {
        assertDoesNotThrow(
                () -> allStatusCombinations((status, conclusion) ->
                        new GHActionRun(
                                0, "", status, conclusion, 0, "", ""
                        ).convertStatus()
                )
        );
    }

    @Test
    void convertingStatusThrowsIllegalStateExceptionIfStringUnknownStatus() {
        var actionRun = new GHActionRun(
                0, "",
                "radom unknown status that definetly does not exist",
                null,
                0, "", ""
        );
        assertThrows(
                IllegalStateException.class,
                actionRun::convertStatus
        );
    }

    @Test
    void convertingUnknownConclusionShouldThrowIllegalStateExeption() {
        var actionRun = new GHActionRun(
                0, "",
                allStatuses.getFirst(),
                "random conclusion that is not known",
                0, "", ""
        );
        assertThrows(
                IllegalStateException.class,
                actionRun::convertStatus
        );
    }

    void allStatusCombinations(BiConsumer<String, String> consumer) {
        allStatuses.forEach(status ->
                allConclusions.forEach(
                        conclusion -> consumer.accept(status, conclusion)
                )
        );
    }
}

