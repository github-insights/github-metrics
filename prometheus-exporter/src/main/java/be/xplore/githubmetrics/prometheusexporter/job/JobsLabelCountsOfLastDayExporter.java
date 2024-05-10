package be.xplore.githubmetrics.prometheusexporter.job;

import be.xplore.githubmetrics.domain.job.GetAllJobsOfLastDayUseCase;
import be.xplore.githubmetrics.domain.job.Job;
import be.xplore.githubmetrics.domain.job.JobConclusion;
import be.xplore.githubmetrics.domain.job.JobStatus;
import be.xplore.githubmetrics.prometheusexporter.ScheduledExporter;
import be.xplore.githubmetrics.prometheusexporter.config.SchedulingProperties;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class JobsLabelCountsOfLastDayExporter implements ScheduledExporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobsLabelCountsOfLastDayExporter.class);
    private final GetAllJobsOfLastDayUseCase getAllJobsOfLastDayUseCase;
    private final MeterRegistry registry;
    private final String cronExpression;

    public JobsLabelCountsOfLastDayExporter(
            GetAllJobsOfLastDayUseCase getAllJobsOfLastDayUseCase,
            SchedulingProperties schedulingProperties,
            MeterRegistry registry
    ) {
        this.getAllJobsOfLastDayUseCase = getAllJobsOfLastDayUseCase;
        this.registry = registry;
        this.cronExpression = schedulingProperties.jobsInterval();
    }

    private void retrieveAndExportLastDaysJobLabelCounts() {
        LOGGER.info("Running scheduled jobs task");

        List<Job> jobs = getAllJobsOfLastDayUseCase.getAllJobsOfLastDay();

        Map<JobLabel, Integer> jobLabelCounts = this.getJobLabelsCounts(jobs);

        LOGGER.debug("Job Metrics to export: {}", jobLabelCounts.size());

        this.publishJobsLabelCountsGauges(jobLabelCounts);

        LOGGER.info("Finished Scheduled Jobs task");
    }

    private void publishJobsLabelCountsGauges(Map<JobLabel, Integer> statuses) {
        for (Map.Entry<JobLabel, Integer> entry : statuses.entrySet()) {
            Gauge.builder("workflow_run_jobs",
                            entry,
                            Map.Entry::getValue)
                    .tag("conclusion", entry.getKey().conclusion().toString())
                    .tag("status", entry.getKey().status().toString())
                    .strongReference(true)
                    .register(this.registry);
        }
    }

    private Map<JobLabel, Integer> createJobLabelsCountsMap() {
        Map<JobLabel, Integer> jobLabelCounts = new HashMap<>();

        Stream.of(JobStatus.values()).forEach(status ->
                Stream.of(JobConclusion.values()).forEach(conclusion ->
                        jobLabelCounts.put(
                                new JobLabel(status, conclusion),
                                0
                        )));

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

    @Override
    public void run() {
        this.retrieveAndExportLastDaysJobLabelCounts();
    }

    @Override
    public String cronExpression() {
        return this.cronExpression;
    }
}
