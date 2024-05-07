package be.xplore.githubmetrics.prometheusexporter.workflowruns;

import be.xplore.githubmetrics.domain.domain.WorkflowRun;
import be.xplore.githubmetrics.domain.usecases.GetAllWorkflowRunsOfLastDayUseCase;
import be.xplore.githubmetrics.prometheusexporter.SchedulingProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkflowRunStatusCountsOfLastDayExporterTest {

    private WorkflowRunStatusCountsOfLastDayExporter workflowRunStatusCountsOfLastDayExporter;
    private GetAllWorkflowRunsOfLastDayUseCase mockUseCase;
    private MeterRegistry registry;

    private List<WorkflowRun> getRuns(WorkflowRun.RunStatus status, int num) {
        return IntStream.range(0, num)
                .mapToObj(n -> new WorkflowRun(0L, "", status, null))
                .toList();
    }

    @BeforeEach
    void setUp() {
        this.registry = new SimpleMeterRegistry();
        this.mockUseCase = Mockito.mock(GetAllWorkflowRunsOfLastDayUseCase.class);
        var mockProperties = Mockito.mock(SchedulingProperties.class);
        Mockito.when(mockProperties.workflowRunsInterval()).thenReturn("");

        this.workflowRunStatusCountsOfLastDayExporter = new WorkflowRunStatusCountsOfLastDayExporter(
                mockUseCase, mockProperties, registry
        );
    }

    @Test
    void workflowRunsWithDifferentStatusesShouldBeCountedCorrectly() {
        Mockito.when(this.mockUseCase.getAllWorkflowRunsOfLastDay()).thenReturn(
                Stream.of(
                                getRuns(WorkflowRun.RunStatus.DONE, 5),
                                getRuns(WorkflowRun.RunStatus.PENDING, 3),
                                getRuns(WorkflowRun.RunStatus.FAILED, 7)
                        ).flatMap(Collection::stream)
                        .collect(Collectors.toList())
        );

        workflowRunStatusCountsOfLastDayExporter.run();
        assertEquals(
                5,
                registry.find("workflow_runs").tag("status", "DONE").gauge().value()
        );
        assertEquals(
                3,
                registry.find("workflow_runs").tag("status", "PENDING").gauge().value()
        );
        assertEquals(
                7,
                registry.find("workflow_runs").tag("status", "FAILED").gauge().value()
        );
    }
}
