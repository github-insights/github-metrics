package be.xplore.githubmetrics.githubadapter.config;

import be.xplore.githubmetrics.githubadapter.mappingclasses.GHActionRunTiming;
import be.xplore.githubmetrics.githubadapter.mappingclasses.GHActionRuns;
import be.xplore.githubmetrics.githubadapter.mappingclasses.GHAppInstallationAccessToken;
import be.xplore.githubmetrics.githubadapter.mappingclasses.GHPullRequest;
import be.xplore.githubmetrics.githubadapter.mappingclasses.GHRepositories;
import be.xplore.githubmetrics.githubadapter.mappingclasses.GHSelfHostedRunners;
import be.xplore.githubmetrics.githubadapter.mappingclasses.GHWorkflowRunJobs;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.Map;

public interface GithubRestClient {

    @PostExchange("/app/installations/{installationId}/access_tokens")
    ResponseEntity<GHAppInstallationAccessToken> getInstallationAccessToken(
            @PathVariable String installationId
    );

    @GetExchange("/installation/repositories")
    ResponseEntity<GHRepositories> getRepositories(
            @RequestParam Map<String, String> requestParams
    );

    @GetExchange("repos/{org}/{repo}/actions/runs")
    ResponseEntity<GHActionRuns> getWorkflowRuns(
            @PathVariable String org,
            @PathVariable String repo,
            @RequestParam Map<String, String> requestParams
    );

    @SuppressWarnings("checkstyle:ParameterNumber")
    @GetExchange("repos/{org}/{repo}/actions/runs/{workflowRunId}/jobs")
    ResponseEntity<GHWorkflowRunJobs> getJobs(
            @PathVariable String org,
            @PathVariable String repo,
            @PathVariable String workflowRunId,
            @RequestParam Map<String, String> requestParams
    );

    @GetExchange("/repos/{org}/{repo}/pulls")
    ResponseEntity<GHPullRequest[]> getPullRequests(
            @PathVariable String org,
            @PathVariable String repo,
            @RequestParam Map<String, String> requestParams
    );

    @GetExchange("repos/{org}/{repo}/actions/runners")
    ResponseEntity<GHSelfHostedRunners> getRepoSelfHostedRunners(
            @PathVariable String org,
            @PathVariable String repo,
            @RequestParam Map<String, String> requestParams
    );

    @GetExchange("orgs/{org}/actions/runners")
    ResponseEntity<GHSelfHostedRunners> getOrgSelfHostedRunners(
            @PathVariable String org,
            @RequestParam Map<String, String> requestParams
    );

    @GetExchange("repos/{org}/{repo}/actions/runs/{workflowRunId}/timing")
    ResponseEntity<GHActionRunTiming> getWorkflowRunBuildTime(
            @PathVariable String org,
            @PathVariable String repo,
            @PathVariable String workflowRunId
    );

}
