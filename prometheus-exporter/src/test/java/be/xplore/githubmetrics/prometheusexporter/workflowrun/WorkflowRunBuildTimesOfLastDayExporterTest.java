package be.xplore.githubmetrics.prometheusexporter.workflowrun;

import be.xplore.githubmetrics.domain.workflowrun.GetAllWorkflowRunBuildTimesOfLastDayUseCase;
import be.xplore.githubmetrics.domain.workflowrun.WorkflowRun;
import be.xplore.githubmetrics.domain.workflowrun.WorkflowRunStatus;
import be.xplore.githubmetrics.prometheusexporter.config.SchedulingProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkflowRunBuildTimesOfLastDayExporterTest {
    private WorkflowRunBuildTimesOfLastDayExporter workflowRunBuildTimesOfLastDayExporter;
    private GetAllWorkflowRunBuildTimesOfLastDayUseCase mockUseCase;

    private MeterRegistry registry;

    private List<WorkflowRun> getRuns(WorkflowRunStatus status, int num) {
        return IntStream.range(0, num)
                .mapToObj(n -> new WorkflowRun(0L, "", status, null, 60))
                .toList();
    }

    @BeforeEach
    void setUp() {
        this.registry = new SimpleMeterRegistry();
        this.mockUseCase = Mockito.mock(GetAllWorkflowRunBuildTimesOfLastDayUseCase.class);
        var mockProperties = Mockito.mock(SchedulingProperties.class);
        Mockito.when(mockProperties.workflowRunBuildTimes()).thenReturn("");

        this.workflowRunBuildTimesOfLastDayExporter = new WorkflowRunBuildTimesOfLastDayExporter(
                mockUseCase, mockProperties, registry
        );
    }

    @Test
    void workflowRunBuildTimesShouldTotalCorrectly() {
        Mockito.when(this.mockUseCase.getAllWorkflowRunBuildTime()).thenReturn(
                Stream.of(
                        getRuns(WorkflowRunStatus.DONE, 5),
                        getRuns(WorkflowRunStatus.PENDING, 3),
                        getRuns(WorkflowRunStatus.FAILED, 7)
                ).flatMap(Collection::stream).toList()
        );

        workflowRunBuildTimesOfLastDayExporter.run();
        assertEquals(
                300.0,
                registry.find("workflow_runs_total_build_times")
                        .tag("status", "DONE")
                        .gauge().value()
        );
        assertEquals(
                180.0,
                registry.find("workflow_runs_total_build_times")
                        .tag("status", "PENDING")
                        .gauge().value()
        );
        assertEquals(
                420.0,
                registry.find("workflow_runs_total_build_times")
                        .tag("status", "FAILED")
                        .gauge().value()
        );
    }

    @Test
    void workflowRunBuildTimesShouldAverageCorrectly() {
        Mockito.when(this.mockUseCase.getAllWorkflowRunBuildTime()).thenReturn(
                Stream.of(
                        getRuns(WorkflowRunStatus.DONE, 5),
                        getRuns(WorkflowRunStatus.PENDING, 3),
                        getRuns(WorkflowRunStatus.FAILED, 7)
                ).flatMap(Collection::stream).toList()
        );

        workflowRunBuildTimesOfLastDayExporter.run();
        assertEquals(
                60.0,
                registry.find("workflow_runs_average_build_times")
                        .tag("status", "DONE")
                        .gauge()
                        .value()
        );
    }
}
