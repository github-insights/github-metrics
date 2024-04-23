package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.domain.WorkflowRun;
import be.xplore.githubmetrics.domain.usecases.ports.in.WorkflowRunsQueryPort;
import be.xplore.githubmetrics.githubadapter.mappingclasses.GHActionRuns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
    public List<WorkflowRun> getLastDaysWorkflows() {
        var parameterMap = new HashMap<String, String>();
        parameterMap.put("created", ">=2024-04-10");
        var responseSpec = githubAdapter.getResponseSpec("/repos/github-insights/github-metrics/actions/runs", parameterMap);
        GHActionRuns actionRuns = responseSpec.body(GHActionRuns.class);
        if (actionRuns == null) {
            throw new RuntimeException("Unexpected error in parsing Workflow Runs");
        }
        List<WorkflowRun> workflowRuns = actionRuns.getWorkFlowRuns();
        LOGGER.debug("number of unique workflow runs: {}", workflowRuns.size());
        return workflowRuns;
    }
}
