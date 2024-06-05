package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.repository.Repository;
import be.xplore.githubmetrics.domain.workflowrun.WorkflowRun;
import be.xplore.githubmetrics.domain.workflowrun.WorkflowRunStatus;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkflowRunsAdapterTest {

    private final Repository repository = new Repository(123L, "github-metrics", "", new ArrayList<>());
    private WireMockServer wireMockServer;
    private WorkflowRunsAdapter workflowRunsAdapter;

    @BeforeEach
    void setupWireMock() {

        this.wireMockServer = TestUtility.getWireMockServer();
        var githubProperties = TestUtility.getNoAuthGithubProperties(wireMockServer.port());
        var restClient = TestUtility.getDefaultRestClientNoAuth(githubProperties);
        var utilities = new GithubApiUtilities();
        workflowRunsAdapter = new WorkflowRunsAdapter(
                githubProperties,
                TestUtility.getApiRateLimitState(),
                restClient,
                utilities,
                TestUtility.getCacheEvictionProperties()
        );
    }

    @AfterEach
    void stopWireMock() {
        wireMockServer.stop();
    }

    @Test
    void workFlowRunsTest() {
        stubFor(
                get(urlMatching(
                        "/repos/github-insights/github-metrics/actions/runs\\?per_page=100&(.*)"))
                        .willReturn(
                                aResponse()
                                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                        .withBodyFile("WorkFlowRunsValidTestData.json")
                        )
        );
        assertFalse(
                workflowRunsAdapter.getLastDaysWorkflowRuns(repository)
                        .isEmpty()
        );
    }

    @Test
    void workFlowRunsInvalidResponseTest() {
        stubFor(get(urlMatching("/repos/github-insights/github-metrics/actions/runs\\?per_page=100&(.*)"))
                .willReturn(
                        aResponse()
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .withBody("")
                )
        );
        assertThrows(
                NullPointerException.class,
                () -> workflowRunsAdapter.getLastDaysWorkflowRuns(repository)
        );
    }

    @Test
    void workflowRunsQueryShouldHaveCorrectStatus() {
        stubFor(get(urlMatching("/repos/github-insights/github-metrics/actions/runs\\?per_page=100&(.*)"))
                .willReturn(
                        aResponse()
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .withBodyFile("WorkFlowRunsStatusTestData.json")
                )
        );

        List<WorkflowRun> workflowRuns = workflowRunsAdapter.getLastDaysWorkflowRuns(repository);
        this.assertDoneStatus(workflowRuns);
        this.assertFailedStatus(workflowRuns);
        this.assertInProgressPendingStatus(workflowRuns);

    }

    void assertDoneStatus(List<WorkflowRun> workflowRuns) {
        assertSame(WorkflowRunStatus.DONE, workflowRuns.get(0).getStatus());
        assertSame(WorkflowRunStatus.DONE, workflowRuns.get(1).getStatus());
    }

    void assertFailedStatus(List<WorkflowRun> workflowRuns) {
        assertSame(WorkflowRunStatus.FAILED, workflowRuns.get(2).getStatus());
        assertSame(WorkflowRunStatus.FAILED, workflowRuns.get(3).getStatus());
        assertSame(WorkflowRunStatus.FAILED, workflowRuns.get(4).getStatus());
        assertSame(WorkflowRunStatus.FAILED, workflowRuns.get(5).getStatus());
        assertSame(WorkflowRunStatus.FAILED, workflowRuns.get(6).getStatus());
        assertSame(WorkflowRunStatus.FAILED, workflowRuns.get(7).getStatus());
        assertSame(WorkflowRunStatus.FAILED, workflowRuns.get(8).getStatus());
    }

    void assertInProgressPendingStatus(List<WorkflowRun> workflowRuns) {
        assertSame(WorkflowRunStatus.IN_PROGRESS, workflowRuns.get(9).getStatus());
        assertSame(WorkflowRunStatus.PENDING, workflowRuns.get(10).getStatus());
        assertSame(WorkflowRunStatus.PENDING, workflowRuns.get(11).getStatus());
        assertSame(WorkflowRunStatus.PENDING, workflowRuns.get(12).getStatus());
        assertSame(WorkflowRunStatus.PENDING, workflowRuns.get(13).getStatus());
    }

}
