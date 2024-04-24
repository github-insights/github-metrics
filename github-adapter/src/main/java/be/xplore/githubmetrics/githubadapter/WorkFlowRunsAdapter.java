package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.domain.WorkflowRun;
import be.xplore.githubmetrics.domain.exceptions.GenericAdapterException;
import be.xplore.githubmetrics.domain.usecases.ports.in.WorkflowRunsQueryPort;
import be.xplore.githubmetrics.githubadapter.config.GithubConfig;
import be.xplore.githubmetrics.githubadapter.exceptions.UnableToParseGHActionRunsException;
import be.xplore.githubmetrics.githubadapter.mappingclasses.GHActionRuns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;

@Service
public class WorkFlowRunsAdapter implements WorkflowRunsQueryPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkFlowRunsAdapter.class);
    private final GithubConfig config;
    private final GithubAdapter githubAdapter;

    public WorkFlowRunsAdapter(GithubConfig config, GithubAdapter githubAdapter) {
        this.config = config;
        this.githubAdapter = githubAdapter;
    }

    @Override
    public List<WorkflowRun> getLastDaysWorkflows() throws GenericAdapterException {
        var parameterMap = new HashMap<String, String>();
        parameterMap.put("created", ">=2024-04-10");
        GHActionRuns actionRuns = githubAdapter.getResponseSpec(
                MessageFormat.format(
                        "{0}/repos/{1}/github-metrics/actions/runs",
                        this.config.host(),
                        this.config.org()
                ),
                new HashMap<>()
        ).body(GHActionRuns.class);
        if (actionRuns == null) {
            throw new UnableToParseGHActionRunsException(
                    "Unexpected error in parsing Workflow Runs"
            );
        }
        List<WorkflowRun> workflowRuns = actionRuns.getWorkFlowRuns();
        LOGGER.debug("number of unique workflow runs: {}", workflowRuns.size());
        return workflowRuns;
    }
}
