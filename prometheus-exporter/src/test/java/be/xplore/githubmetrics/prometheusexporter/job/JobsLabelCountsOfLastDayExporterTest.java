package be.xplore.githubmetrics.prometheusexporter.job;

import be.xplore.githubmetrics.domain.job.GetAllJobsOfLastDayUseCase;
import be.xplore.githubmetrics.domain.job.Job;
import be.xplore.githubmetrics.domain.job.JobConclusion;
import be.xplore.githubmetrics.domain.job.JobStatus;
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

class JobsLabelCountsOfLastDayExporterTest {

    private static final String WORKFLOW_RUN_JOBS = "workflow_run_jobs";
    private static final String STATUS = "status";
    private static final String CONCLUSION = "conclusion";
    private JobsLabelCountsOfLastDayExporter jobsLabelCountsOfLastDayExporter;
    private GetAllJobsOfLastDayUseCase mockUseCase;
    private MeterRegistry registry;

    private List<Job> getJobs(JobStatus status, JobConclusion conclusion, int num) {
        return IntStream.range(0, num)
                .mapToObj(n -> new Job(0L, 0L, status, conclusion))
                .toList();
    }

    @BeforeEach
    void setUp() {
        this.registry = new SimpleMeterRegistry();
        this.mockUseCase = Mockito.mock(GetAllJobsOfLastDayUseCase.class);
        var mockProperties = Mockito.mock(SchedulingProperties.class);
        Mockito.when(mockProperties.workflowRuns()).thenReturn("");

        this.jobsLabelCountsOfLastDayExporter = new JobsLabelCountsOfLastDayExporter(
                this.mockUseCase, mockProperties, registry
        );
    }

    @Test
    void jobsWithDifferentStatusesAndConclusionsShouldBeCountedCorrectly() {
        Mockito.when(this.mockUseCase.getAllJobsOfLastDay()).thenReturn(
                Stream.of(
                                getJobs(JobStatus.DONE, JobConclusion.FAILURE, 1),
                                getJobs(JobStatus.IN_PROGRESS, JobConclusion.FAILURE, 2),
                                getJobs(JobStatus.PENDING, JobConclusion.FAILURE, 3),
                                getJobs(JobStatus.DONE, JobConclusion.NEUTRAL, 4),
                                getJobs(JobStatus.IN_PROGRESS, JobConclusion.NEUTRAL, 5),
                                getJobs(JobStatus.PENDING, JobConclusion.NEUTRAL, 6),
                                getJobs(JobStatus.DONE, JobConclusion.SUCCESS, 7),
                                getJobs(JobStatus.IN_PROGRESS, JobConclusion.SUCCESS, 8),
                                getJobs(JobStatus.PENDING, JobConclusion.SUCCESS, 9)
                        ).flatMap(Collection::stream)
                        .toList()
        );

        jobsLabelCountsOfLastDayExporter.run();
        assertFailureCounts();
        assertNeutralCounts();
        assertSuccessCounts();
    }

    private void assertSuccessCounts() {
        assertEquals(
                7,
                registry.find(WORKFLOW_RUN_JOBS).tags(
                        STATUS, "DONE", CONCLUSION, "SUCCESS"
                ).gauge().value()
        );
        assertEquals(
                8,
                registry.find(WORKFLOW_RUN_JOBS).tags(
                        STATUS, "IN_PROGRESS", CONCLUSION, "SUCCESS"
                ).gauge().value()
        );
        assertEquals(
                9,
                registry.find(WORKFLOW_RUN_JOBS).tags(
                        STATUS, "PENDING", CONCLUSION, "SUCCESS"
                ).gauge().value()
        );
    }

    private void assertNeutralCounts() {
        assertEquals(
                4,
                registry.find(WORKFLOW_RUN_JOBS).tags(
                        STATUS, "DONE", CONCLUSION, "NEUTRAL"
                ).gauge().value()
        );
        assertEquals(
                5,
                registry.find(WORKFLOW_RUN_JOBS).tags(
                        STATUS, "IN_PROGRESS", CONCLUSION, "NEUTRAL"
                ).gauge().value()
        );
        assertEquals(
                6,
                registry.find(WORKFLOW_RUN_JOBS).tags(
                        STATUS, "PENDING", CONCLUSION, "NEUTRAL"
                ).gauge().value()
        );
    }

    private void assertFailureCounts() {
        assertEquals(
                1,
                registry.find(WORKFLOW_RUN_JOBS).tags(
                        STATUS, "DONE", CONCLUSION, "FAILURE"
                ).gauge().value()
        );
        assertEquals(
                2,
                registry.find(WORKFLOW_RUN_JOBS).tags(
                        STATUS, "IN_PROGRESS", CONCLUSION, "FAILURE"
                ).gauge().value()
        );
        assertEquals(
                3,
                registry.find(WORKFLOW_RUN_JOBS).tags(
                        STATUS, "PENDING", CONCLUSION, "FAILURE"
                ).gauge().value()
        );
    }

}
