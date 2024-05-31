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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkflowRunBuildTimesAdapterTest {
    private WorkflowRun workflowRun;

    private WireMockServer wireMockServer;
    private WorkflowRunBuildTimesAdapter buildTimesAdapter;

    @Test
    void workflowRunBuildTimesAdapterShouldReturnABuildTime() {
        stubFor(
                get(urlEqualTo(
                        "/repos/github-insights/github-metrics/actions/runs/8828175949/timing")
                ).willReturn(
                        aResponse()
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .withBodyFile("WorkflowRunBuildTime.json")
                ));

        this.workflowRun.setBuildTime(buildTimesAdapter.getWorkflowRunBuildTimes(this.workflowRun));
        assertEquals(136_000, this.workflowRun.getBuildTime());

    }

    @Test
    void ifNullResponseFireException() {
        stubFor(
                get(urlEqualTo(
                        "/repos/github-insights/github-metrics/actions/runs/8828175949/timing")
                ).willReturn(
                        aResponse()
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .withBody("")));
        assertThrows(
                NullPointerException.class,
                () -> {
                    buildTimesAdapter.getWorkflowRunBuildTimes(this.workflowRun);
                }
        );
    }

    @BeforeEach
    void setup() {
        wireMockServer = TestUtility.getWireMockServer();

        var githubProperties = TestUtility.getNoAuthGithubProperties(wireMockServer.port());
        var tokenRestClient = TestUtility.getDefaultRestClientNoAuth(githubProperties);

        buildTimesAdapter = new WorkflowRunBuildTimesAdapter(
                githubProperties, tokenRestClient,
                TestUtility.getCacheEvictionProperties(),
                TestUtility.getApiRateLimitState(),
                new GithubApiUtilities()
        );

        this.workflowRun = new WorkflowRun(
                8_828_175_949L,
                "workflow run",
                WorkflowRunStatus.DONE,
                new Repository(
                        8_828_175_949L,
                        "github-metrics",
                        "github-metrics",
                        new ArrayList<String>()
                )
        );
    }

    @AfterEach
    void takedown() {
        wireMockServer.stop();
    }
}
