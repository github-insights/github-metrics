package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.repository.Repository;
import be.xplore.githubmetrics.domain.workflowrun.WorkflowRun;
import be.xplore.githubmetrics.domain.workflowrun.WorkflowRunsQueryPort;
import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import be.xplore.githubmetrics.githubadapter.mappingclasses.GHActionRuns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

@Service
public class WorkflowRunsAdapter implements WorkflowRunsQueryPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowRunsAdapter.class);
    private final GithubProperties githubProperties;
    private final RestClient restClient;

    public WorkflowRunsAdapter(
            GithubProperties githubProperties,
            @Qualifier("defaultRestClient") RestClient restClient
    ) {
        this.githubProperties = githubProperties;
        this.restClient = restClient;
    }

    private String getWorkflowRunsApiPath(String repoName) {
        return MessageFormat.format(
                "repos/{0}/{1}/actions/runs",
                this.githubProperties.org(),
                repoName
        );
    }

    @Cacheable("WorkflowRuns")
    @Override
    public List<WorkflowRun> getLastDaysWorkflowRuns(Repository repository) {
        var parameters = new HashMap<String, String>();
        parameters.put(
                "created",
                ">=" + LocalDate.now().minusDays(1)
                        .format(DateTimeFormatter.ISO_LOCAL_DATE)
        );

        var response = this.restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(this.getWorkflowRunsApiPath(repository.getName()));
                    for (final var parameter : parameters.entrySet()) {
                        uriBuilder.queryParam(parameter.getKey(), parameter.getValue());
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .body(GHActionRuns.class);

        List<WorkflowRun> workflowRuns = response.getWorkFlowRuns(repository);
        LOGGER.debug("number of unique workflow runs: {}", workflowRuns.size());
        return workflowRuns;
    }
}
