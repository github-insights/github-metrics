package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.domain.Repository;
import be.xplore.githubmetrics.domain.domain.WorkflowRun;
import be.xplore.githubmetrics.domain.queries.WorkflowRunsQueryPort;
import be.xplore.githubmetrics.githubadapter.exceptions.UnableToParseGHActionRunsException;
import be.xplore.githubmetrics.githubadapter.mappingclasses.GHActionRuns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

@Service
public class WorkFlowRunsAdapter implements WorkflowRunsQueryPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkFlowRunsAdapter.class);
    private final GithubAdapter githubAdapter;

    public WorkFlowRunsAdapter(GithubAdapter githubAdapter) {
        this.githubAdapter = githubAdapter;
    }

    @Override
    public List<WorkflowRun> getLastDaysWorkflows(Repository repository) {
        var parameterMap = new HashMap<String, String>();
        parameterMap.put(
                "created",
                ">=" + LocalDate.now().minusDays(1)
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        );

        var responseSpec = githubAdapter.getResponseSpec(
                MessageFormat.format(
                        "repos/{0}/{1}/actions/runs",
                        this.githubAdapter.getConfig().org(),
                        repository.getName()
                ),
                parameterMap
        ).body(GHActionRuns.class);

        if (responseSpec == null) {
            throw new UnableToParseGHActionRunsException(
                    "Unexpected error in parsing Workflow Runs"
            );
        }
        List<WorkflowRun> workflowRuns = responseSpec.getWorkFlowRuns(repository);
        LOGGER.debug("number of unique workflow runs: {}", workflowRuns.size());
        return workflowRuns;
    }
}
