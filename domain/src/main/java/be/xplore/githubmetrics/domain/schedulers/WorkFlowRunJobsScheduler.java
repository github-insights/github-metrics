package be.xplore.githubmetrics.domain.schedulers;

import be.xplore.githubmetrics.domain.domain.Job;
import be.xplore.githubmetrics.domain.exports.WorkflowRunJobsExportPort;
import be.xplore.githubmetrics.domain.providers.ports.RepositoriesProvider;
import be.xplore.githubmetrics.domain.providers.ports.WorkflowRunsProvider;
import be.xplore.githubmetrics.domain.queries.WorkFlowRunJobsQueryPort;
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

    private final WorkflowRunsProvider workflowRunsProvider;
    private final RepositoriesProvider repositoriesProvider;
    private final WorkFlowRunJobsQueryPort workFlowRunJobsQueryPort;
    private final List<WorkflowRunJobsExportPort> workflowRunJobsExportPorts;

    public WorkFlowRunJobsScheduler(
            WorkflowRunsProvider workflowRunsProvider,
            RepositoriesProvider repositoriesProvider,
            WorkFlowRunJobsQueryPort workFlowRunJobsQueryPort, List<WorkflowRunJobsExportPort> workflowRunJobsExportPorts
    ) {
        this.workflowRunsProvider = workflowRunsProvider;
        this.repositoriesProvider = repositoriesProvider;
        this.workFlowRunJobsQueryPort = workFlowRunJobsQueryPort;
        this.workflowRunJobsExportPorts = workflowRunJobsExportPorts;
    }

    @Scheduled(fixedRate = 60, timeUnit = TimeUnit.SECONDS)
    @Override
    public void retrieveAndExportJobs() {
        LOGGER.info("Running scheduled jobs task");

        var allRepositories = this.repositoriesProvider.getAllRepositories();

        var jobLabelCounts = createJobLabelsCountsMap();

        allRepositories.forEach(repository ->
                this.workflowRunsProvider.getLastDaysWorkflowRuns(repository.getName())
                        .forEach(workflowRun ->
                                updateJobLabelsCounts(
                                        workFlowRunJobsQueryPort.getWorkFlowRunJobs(
                                                repository.getName(),
                                                workflowRun.getId()),
                                        jobLabelCounts
                                )));

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

    private void updateJobLabelsCounts(
            List<Job> jobs,
            Map<JobLabels, Integer> jobLabelsCounts
    ) {
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
    }

    public record JobLabels(
            Job.JobStatus status,
            Job.JobConclusion conclusion
    ) {

    }
}
