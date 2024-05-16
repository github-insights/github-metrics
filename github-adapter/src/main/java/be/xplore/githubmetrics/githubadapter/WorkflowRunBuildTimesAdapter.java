package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.workflowrun.WorkflowRun;
import be.xplore.githubmetrics.domain.workflowrun.WorkflowRunBuildTimesQueryPort;
import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import be.xplore.githubmetrics.githubadapter.mappingclasses.GHActionRunTiming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.text.MessageFormat;

@Service
public class WorkflowRunBuildTimesAdapter implements WorkflowRunBuildTimesQueryPort {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowRunBuildTimesAdapter.class);
    private final GithubProperties githubProperties;
    private final RestClient restClient;

    public WorkflowRunBuildTimesAdapter(
            GithubProperties githubProperties,
            @Qualifier("defaultRestClient") RestClient restClient
    ) {
        this.githubProperties = githubProperties;
        this.restClient = restClient;
    }

    @Cacheable("WorkflowRunBuildTimes")
    @Override
    public int getWorkflowRunBuildTimes(WorkflowRun workflowRun) {
        LOGGER.info(
                "Fetching fresh BuildTimes for WorkflowRun {}.",
                workflowRun.getId()
        );

        var buildTime = this.restClient.get()
                .uri(this.getBuildTimesApiPath(workflowRun))
                .retrieve()
                .body(GHActionRunTiming.class)
                .run_duration_ms();

        LOGGER.debug(
                "Response for the BuildTimes fetch of WorkflowRun {} returned BuildTime of {}",
                workflowRun.getId(),
                buildTime
        );

        return buildTime;
    }

    private String getBuildTimesApiPath(WorkflowRun workflowRun) {
        return MessageFormat.format(
                "repos/{0}/{1}/actions/runs/{2,number,#}/timing",
                this.githubProperties.org(),
                workflowRun.getRepository().getName(),
                workflowRun.getId()
        );
    }
}
