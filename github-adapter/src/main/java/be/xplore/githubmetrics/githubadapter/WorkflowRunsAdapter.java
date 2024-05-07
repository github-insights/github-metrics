package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.repository.Repository;
import be.xplore.githubmetrics.domain.workflowrun.WorkflowRunsQueryPort;
import be.xplore.githubmetrics.domain.workflowrun.model.WorkflowRun;
import be.xplore.githubmetrics.githubadapter.mappingclasses.GHActionRuns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

@Service
public class WorkflowRunsAdapter implements WorkflowRunsQueryPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowRunsAdapter.class);
    private final GithubAdapter githubAdapter;

    public WorkflowRunsAdapter(GithubAdapter githubAdapter) {
        this.githubAdapter = githubAdapter;
    }

    @Cacheable("WorkflowRuns")
    @Override
    public List<WorkflowRun> getLastDaysWorkflowRuns(Repository repository) {
        var parameterMap = new HashMap<String, String>();
        parameterMap.put(
                "created",
                ">=" + LocalDate.now().minusDays(1)
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        );

        var ghWorkflowRuns = GithubAdapter.getBody(
                githubAdapter.getResponseSpec(
                        MessageFormat.format(
                                "repos/{0}/{1}/actions/runs",
                                this.githubAdapter.getConfig().org(),
                                repository.getName()
                        ),
                        parameterMap
                ),
                GHActionRuns.class
        );

        List<WorkflowRun> workflowRuns = ghWorkflowRuns.getWorkFlowRuns(repository);
        LOGGER.debug("number of unique workflow runs: {}", workflowRuns.size());
        return workflowRuns;
    }
}
