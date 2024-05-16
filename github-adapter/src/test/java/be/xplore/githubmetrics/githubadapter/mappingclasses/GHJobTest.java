package be.xplore.githubmetrics.githubadapter.mappingclasses;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GHJobTest {

    private final List<String> allStatuses = List.of("requested", "queued",
            "pending", "in_progress", "completed"
    );
    private List<String> allConclusions = List.of("success", "failure",
            "cancelled", "timed_out", "action_required", "neutral", "skipped"
    );

    @BeforeEach
    void setUp() {
        this.allConclusions = new ArrayList<>(this.allConclusions);
        this.allConclusions.add(null);
    }

    @Test
    void allValidStringStatusesAndConclusionCombinationsShouldNotThrow() {
        var allJobCombinations = allStatuses.stream().map(status ->
                allConclusions.stream().map(conclusion ->
                        new GHJob(0, 0, status, conclusion)
                ).toList()
        ).flatMap(List::stream).toList();

        assertDoesNotThrow(() -> allJobCombinations.forEach(GHJob::getJob));
    }

    @Test
    void invalidStatusShouldThrowIllegalStateException() {
        var ghJob = new GHJob(0, 0, "test-status", allConclusions.getFirst());
        assertThrows(
                IllegalStateException.class,
                ghJob::getJob
        );
    }

    @Test
    void invalidConclusionShouldThrowIllegalStateException() {
        var ghJob = new GHJob(0, 0, allStatuses.getFirst(), "test-status");
        assertThrows(
                IllegalStateException.class,
                ghJob::getJob
        );
    }
}
