package be.xplore.githubmetrics.domain.schedulers;

import be.xplore.githubmetrics.domain.domain.WorkflowRun;
import be.xplore.githubmetrics.domain.exports.WorkflowRunsExportPort;
import be.xplore.githubmetrics.domain.providers.usecases.GetAllWorkflowRunsOfLastDayUseCase;
import be.xplore.githubmetrics.domain.schedulers.ports.WorkflowRunsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Component
public class WorkflowRunsRequestScheduler implements WorkflowRunsUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowRunsRequestScheduler.class);
    private final GetAllWorkflowRunsOfLastDayUseCase getAllWorkflowRunsOfLastDayUseCase;
    private final List<WorkflowRunsExportPort> workflowRunsExportPorts;

    public WorkflowRunsRequestScheduler(
            GetAllWorkflowRunsOfLastDayUseCase getAllWorkflowRunsOfLastDayUseCase, List<WorkflowRunsExportPort> workflowRunsExportPorts
    ) {
        this.getAllWorkflowRunsOfLastDayUseCase = getAllWorkflowRunsOfLastDayUseCase;
        this.workflowRunsExportPorts = workflowRunsExportPorts;
    }

    @Scheduled(fixedRate = 200, timeUnit = TimeUnit.SECONDS)
    @Override
    public void retrieveAndExportWorkflowRuns() {
        LOGGER.info("Running scheduled workflow runs task.");
        List<WorkflowRun> workflowRuns
                = getAllWorkflowRunsOfLastDayUseCase.getAllWorkflowRunsOfLastDay();

        Map<WorkflowRun.RunStatus, Integer> workflowRunsStatusCountsMap
                = this.getStatusCounts(workflowRuns);

        this.workflowRunsExportPorts.forEach(port ->
                port.exportWorkflowRunsStatusCounts(workflowRunsStatusCountsMap));

        LOGGER.info("Finished scheduled workflow runs task");
    }

    private Map<WorkflowRun.RunStatus, Integer> createStatusCountsMap() {
        EnumMap<WorkflowRun.RunStatus, Integer> workflowRunStatusCountsMap = new EnumMap<>(WorkflowRun.RunStatus.class);

        Stream.of(WorkflowRun.RunStatus.values()).forEach(
                runStatus -> workflowRunStatusCountsMap.put(runStatus, 0));

        return workflowRunStatusCountsMap;
    }

    private Map<WorkflowRun.RunStatus, Integer> getStatusCounts(
            List<WorkflowRun> workflowRuns
    ) {
        Map<WorkflowRun.RunStatus, Integer> workflowRunStatusCountsMap
                = this.createStatusCountsMap();

        workflowRuns.forEach(workflowRun ->
                workflowRunStatusCountsMap.put(
                        workflowRun.getStatus(),
                        1 + workflowRunStatusCountsMap.get(workflowRun.getStatus())
                )
        );

        return workflowRunStatusCountsMap;
    }

}
