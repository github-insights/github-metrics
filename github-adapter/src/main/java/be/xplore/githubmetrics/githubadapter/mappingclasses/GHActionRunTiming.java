package be.xplore.githubmetrics.githubadapter.mappingclasses;

public record GHActionRunTiming(
        int run_duration_ms
) {

    public static final String PATH = "repos/{org}/{repo}/actions/runs/{workflowRunId}/timing";
}
