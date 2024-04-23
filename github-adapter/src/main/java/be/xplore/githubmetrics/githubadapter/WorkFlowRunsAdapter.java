package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.domain.WorkflowRun;
import be.xplore.githubmetrics.domain.usecases.ports.in.WorkflowRunsQueryPort;
import be.xplore.githubmetrics.githubadapter.config.GithubConfig;
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
    public List<WorkflowRun> getLastDaysWorkflows() {
        var responseSpec = githubAdapter.getResponseSpec(
                MessageFormat.format(
                        "/repos/{0}/github-metrics/actions/runs",
                        this.config.org()
                ),
                new HashMap<String, String>()
        );
        GHActionRuns actionRuns = responseSpec.body(GHActionRuns.class);
        if (actionRuns == null) {
            throw new RuntimeException("Unexpected error in parsing Workflow Runs");
        }
        List<WorkflowRun> workflowRuns = actionRuns.getWorkFlowRuns();
        LOGGER.debug("number of unique workflow runs: {}", workflowRuns.size());
        return workflowRuns;
    }
}
