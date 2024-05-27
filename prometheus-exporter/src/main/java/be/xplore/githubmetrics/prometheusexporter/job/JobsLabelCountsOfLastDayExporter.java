package be.xplore.githubmetrics.prometheusexporter.job;

import be.xplore.githubmetrics.domain.job.GetAllJobsOfLastDayUseCase;
import be.xplore.githubmetrics.domain.job.Job;
import be.xplore.githubmetrics.domain.job.JobConclusion;
import be.xplore.githubmetrics.domain.job.JobStatus;
import be.xplore.githubmetrics.prometheusexporter.ScheduledExporter;
import be.xplore.githubmetrics.prometheusexporter.config.SchedulingProperties;
import be.xplore.githubmetrics.prometheusexporter.features.FeatureAssociation;
import be.xplore.githubmetrics.prometheusexporter.features.Features;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

@Service
public class JobsLabelCountsOfLastDayExporter implements ScheduledExporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobsLabelCountsOfLastDayExporter.class);
    private static final String JOBS_GAUGE_NAME = "workflow_run_jobs";
    private final GetAllJobsOfLastDayUseCase getAllJobsOfLastDayUseCase;
    private final MeterRegistry registry;
    private final String cronExpression;
    private final Map<JobLabel, AtomicInteger> jobCountGauges = new HashMap<>();

    public JobsLabelCountsOfLastDayExporter(
            GetAllJobsOfLastDayUseCase getAllJobsOfLastDayUseCase,
            SchedulingProperties schedulingProperties,
            MeterRegistry registry
    ) {
        this.getAllJobsOfLastDayUseCase = getAllJobsOfLastDayUseCase;
        this.registry = registry;
        this.cronExpression = schedulingProperties.jobs();
        this.initJobCountGauges();
    }

    private void retrieveAndExportLastDaysJobLabelCounts() {
        LOGGER.trace("LastDaysJobLabelCounts scheduled task is running.");

        List<Job> jobs = getAllJobsOfLastDayUseCase.getAllJobsOfLastDay();

        Map<JobLabel, Integer> jobLabelCounts = this.getJobLabelsCounts(jobs);

        jobLabelCounts.forEach((entry, value) ->
                jobCountGauges.get(entry).set(value)
        );

        LOGGER.trace(
                "LastDaysJobLabelCounts scheduled task finished with {} JobLabel combinations.",
                jobLabelCounts.size()
        );
    }

    private Map<JobLabel, Integer> createJobLabelsCountsMap() {
        Map<JobLabel, Integer> jobLabelCounts = new HashMap<>();

        this.allStateCombinations((status, conclusion) ->
                jobLabelCounts.put(new JobLabel(status, conclusion), 0)
        );

        return jobLabelCounts;
    }

    private Map<JobLabel, Integer> getJobLabelsCounts(
            List<Job> jobs
    ) {
        Map<JobLabel, Integer> jobLabelsCounts = this.createJobLabelsCountsMap();

        jobs.forEach(job -> {
            JobLabel jobLabel = new JobLabel(
                    job.getStatus(),
                    job.getConclusion()
            );
            jobLabelsCounts.put(
                    jobLabel,
                    1 + jobLabelsCounts.get(jobLabel)
            );
        });
        return jobLabelsCounts;
    }

    private void initJobCountGauges() {
        this.allStateCombinations((status, conclusion) -> {
            var atomicInteger = new AtomicInteger();
            Gauge.builder(
                            JOBS_GAUGE_NAME,
                            () -> atomicInteger)
                    .tag("conclusion", conclusion.toString())
                    .tag("status", status.toString())
                    .strongReference(true)
                    .register(this.registry);
            this.jobCountGauges.put(
                    new JobLabel(status, conclusion),
                    atomicInteger
            );
        });

    }

    @Override
    @FeatureAssociation(value = Features.EXPORTER_JOBS_FEATURE)
    public void run() {
        this.retrieveAndExportLastDaysJobLabelCounts();
    }

    @Override
    public String cronExpression() {
        return this.cronExpression;
    }

    private void allStateCombinations(BiConsumer<JobStatus, JobConclusion> function) {
        Stream.of(JobStatus.values()).forEach(status ->
                Stream.of(JobConclusion.values()).forEach(conclusion ->
                        function.accept(status, conclusion)
                ));
    }
}
