package be.xplore.githubmetrics.domain.schedulers;

import be.xplore.githubmetrics.domain.domain.Job;
import be.xplore.githubmetrics.domain.exports.WorkflowRunJobsExportPort;
import be.xplore.githubmetrics.domain.providers.usecases.GetAllJobsOfLastDayUseCase;
import be.xplore.githubmetrics.domain.schedulers.ports.JobsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Component
public class WorkFlowRunJobsScheduler implements JobsUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkFlowRunJobsScheduler.class);
    private final GetAllJobsOfLastDayUseCase getAllJobsOfLastDayUseCase;
    private final List<WorkflowRunJobsExportPort> workflowRunJobsExportPorts;

    public WorkFlowRunJobsScheduler(
            GetAllJobsOfLastDayUseCase getAllJobsOfLastDayUseCase, List<WorkflowRunJobsExportPort> workflowRunJobsExportPorts
    ) {
        this.getAllJobsOfLastDayUseCase = getAllJobsOfLastDayUseCase;
        this.workflowRunJobsExportPorts = workflowRunJobsExportPorts;
    }

    @Scheduled(fixedRate = 60, timeUnit = TimeUnit.SECONDS)
    @Override
    public void retrieveAndExportJobs() {
        LOGGER.info("Running scheduled jobs task");

        List<Job> jobs = getAllJobsOfLastDayUseCase.getAllJobsOfLastDay();

        Map<JobLabels, Integer> jobLabelCounts = this.getJobLabelsCounts(jobs);

        LOGGER.debug("Job Metrics to export: {}", jobLabelCounts.size());

        workflowRunJobsExportPorts.forEach(port ->
                port.exportWorkflowRunJobsLabelsCounts(jobLabelCounts));

        LOGGER.info("Finished Scheduled workflow runs task");
    }

    private Map<JobLabels, Integer> createJobLabelsCountsMap() {
        Map<JobLabels, Integer> jobLabelCounts = new HashMap<>();

        Stream.of(Job.JobStatus.values()).forEach(status ->
                Stream.of(Job.JobConclusion.values()).forEach(conclusion ->
                        jobLabelCounts.put(
                                new JobLabels(status, conclusion),
                                0
                        )));

        return jobLabelCounts;
    }

    private Map<JobLabels, Integer> getJobLabelsCounts(
            List<Job> jobs
    ) {
        Map<JobLabels, Integer> jobLabelsCounts = this.createJobLabelsCountsMap();

        jobs.forEach(job -> {
            JobLabels jobLabels = new JobLabels(
                    job.getStatus(),
                    job.getConclusion()
            );
            jobLabelsCounts.put(
                    jobLabels,
                    1 + jobLabelsCounts.get(jobLabels)
            );
        });
        return jobLabelsCounts;
    }

    public record JobLabels(
            Job.JobStatus status,
            Job.JobConclusion conclusion
    ) {

    }
}
