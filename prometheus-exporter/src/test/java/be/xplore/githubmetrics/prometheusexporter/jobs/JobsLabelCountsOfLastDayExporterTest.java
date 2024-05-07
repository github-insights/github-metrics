package be.xplore.githubmetrics.prometheusexporter.jobs;

import be.xplore.githubmetrics.domain.domain.Job;
import be.xplore.githubmetrics.domain.usecases.GetAllJobsOfLastDayUseCase;
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

class JobsLabelCountsOfLastDayExporterTest {

    private JobsLabelCountsOfLastDayExporter jobsLabelCountsOfLastDayExporter;
    private GetAllJobsOfLastDayUseCase mockUseCase;
    private MeterRegistry registry;

    private List<Job> getJobs(Job.JobStatus status, Job.JobConclusion conclusion, int num) {
        return IntStream.range(0, num)
                .mapToObj(n -> new Job(0L, 0L, status, conclusion))
                .toList();
    }

    @BeforeEach
    void setUp() {
        this.registry = new SimpleMeterRegistry();
        this.mockUseCase = Mockito.mock(GetAllJobsOfLastDayUseCase.class);
        var mockProperties = Mockito.mock(SchedulingProperties.class);
        Mockito.when(mockProperties.workflowRunsInterval()).thenReturn("");

        this.jobsLabelCountsOfLastDayExporter = new JobsLabelCountsOfLastDayExporter(
                mockUseCase, mockProperties, registry
        );
    }

    @Test
    void jobsWithDifferentStatusesAndConclusionsShouldBeCountedCorrectly() {
        Mockito.when(this.mockUseCase.getAllJobsOfLastDay()).thenReturn(
                Stream.of(
                                getJobs(Job.JobStatus.DONE, Job.JobConclusion.FAILURE, 1),
                                getJobs(Job.JobStatus.IN_PROGRESS, Job.JobConclusion.FAILURE, 2),
                                getJobs(Job.JobStatus.PENDING, Job.JobConclusion.FAILURE, 3),
                                getJobs(Job.JobStatus.DONE, Job.JobConclusion.NEUTRAL, 4),
                                getJobs(Job.JobStatus.IN_PROGRESS, Job.JobConclusion.NEUTRAL, 5),
                                getJobs(Job.JobStatus.PENDING, Job.JobConclusion.NEUTRAL, 6),
                                getJobs(Job.JobStatus.DONE, Job.JobConclusion.SUCCESS, 7),
                                getJobs(Job.JobStatus.IN_PROGRESS, Job.JobConclusion.SUCCESS, 8),
                                getJobs(Job.JobStatus.PENDING, Job.JobConclusion.SUCCESS, 9)
                        ).flatMap(Collection::stream)
                        .collect(Collectors.toList())
        );

        jobsLabelCountsOfLastDayExporter.run();
        assertFailureCounts();
        assertNeutralCounts();
        assertSuccessCounts();
    }

    private void assertSuccessCounts() {
        assertEquals(
                7,
                registry.find("workflow_run_jobs").tags(
                        "status", "DONE", "conclusion", "SUCCESS"
                ).gauge().value()
        );
        assertEquals(
                8,
                registry.find("workflow_run_jobs").tags(
                        "status", "IN_PROGRESS", "conclusion", "SUCCESS"
                ).gauge().value()
        );
        assertEquals(
                9,
                registry.find("workflow_run_jobs").tags(
                        "status", "PENDING", "conclusion", "SUCCESS"
                ).gauge().value()
        );
    }

    private void assertNeutralCounts() {
        assertEquals(
                4,
                registry.find("workflow_run_jobs").tags(
                        "status", "DONE", "conclusion", "NEUTRAL"
                ).gauge().value()
        );
        assertEquals(
                5,
                registry.find("workflow_run_jobs").tags(
                        "status", "IN_PROGRESS", "conclusion", "NEUTRAL"
                ).gauge().value()
        );
        assertEquals(
                6,
                registry.find("workflow_run_jobs").tags(
                        "status", "PENDING", "conclusion", "NEUTRAL"
                ).gauge().value()
        );
    }

    private void assertFailureCounts() {
        assertEquals(
                1,
                registry.find("workflow_run_jobs").tags(
                        "status", "DONE", "conclusion", "FAILURE"
                ).gauge().value()
        );
        assertEquals(
                2,
                registry.find("workflow_run_jobs").tags(
                        "status", "IN_PROGRESS", "conclusion", "FAILURE"
                ).gauge().value()
        );
        assertEquals(
                3,
                registry.find("workflow_run_jobs").tags(
                        "status", "PENDING", "conclusion", "FAILURE"
                ).gauge().value()
        );
    }

}
