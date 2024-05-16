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
import org.springframework.http.ResponseEntity;
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
    private final GithubApiUtilities utilities;

    public WorkflowRunsAdapter(
            GithubProperties githubProperties,
            @Qualifier("defaultRestClient") RestClient restClient,
            GithubApiUtilities utilities
    ) {
        this.githubProperties = githubProperties;
        this.restClient = restClient;
        this.utilities = utilities;
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
        LOGGER.info("Fetching fresh WorkflowRuns for Repository {}.", repository.getId());
        var parameters = new HashMap<String, String>();
        parameters.put(
                "created",
                ">=" + LocalDate.now().minusDays(1)
                        .format(DateTimeFormatter.ISO_LOCAL_DATE)
        );
        parameters.put("per_page", "100");

        ResponseEntity<GHActionRuns> responseEntity = this.restClient.get()
                .uri(utilities.setPathAndParameters(
                        this.getWorkflowRunsApiPath(repository.getName()),
                        parameters
                ))
                .retrieve()
                .toEntity(GHActionRuns.class);

        var workflowRuns = this.utilities.followPaginationLink(
                responseEntity,
                actionRun -> actionRun.getWorkFlowRuns(repository),
                GHActionRuns.class
        );

        LOGGER.debug(
                "Response for the WorkflowRuns fetch of Repository {} returned {} WorkflowRuns.",
                repository.getId(),
                workflowRuns.size()
        );
        return workflowRuns;
    }
}
