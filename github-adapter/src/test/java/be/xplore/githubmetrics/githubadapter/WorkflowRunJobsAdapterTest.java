package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.domain.Job;
import be.xplore.githubmetrics.domain.exceptions.GenericAdapterException;
import be.xplore.githubmetrics.githubadapter.config.GithubConfig;
import be.xplore.githubmetrics.githubadapter.config.GithubRestClientConfiguration;
import be.xplore.githubmetrics.githubadapter.exceptions.UnableToParseGithubResponseException;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkflowRunJobsAdapterTest {
    private WireMockServer wireMockServer;
    private WorkflowRunJobsAdapter workFlowRunJobsAdapter;

    @BeforeEach
    void setupWireMock() {
        wireMockServer = new WireMockServer(
                wireMockConfig().dynamicPort()
        );

        wireMockServer.start();
        GithubConfig githubConfig = new GithubConfig(
                "http",
                "localhost",
                String.valueOf(wireMockServer.port()),
                "token",
                "github-insights"
        );
        workFlowRunJobsAdapter = new WorkflowRunJobsAdapter(
                new GithubAdapter(
                        githubConfig,
                        new GithubRestClientConfiguration().getGithubRestClient(githubConfig)
                ));
        configureFor("localhost", wireMockServer.port());
    }

    @AfterEach
    void stopWireMock() {
        wireMockServer.stop();
    }

    @Test
    void workflowRunJobsAdapterShouldReturnCorrectNumberOfJobs() {
        stubFor(
                get(urlEqualTo(
                        "/repos/github-insights/github-metrics/actions/runs/8828175949/jobs")
                ).willReturn(
                        aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBodyFile("WorkFlowRunJobsValidTestData.json")
                ));

        List<Job> workFlowRunJobs = workFlowRunJobsAdapter
                .getWorkFlowRunJobs(
                        "github-metrics",
                        8828175949L);
        assertEquals(1, workFlowRunJobs.size());
    }

    @Test
    void assertExceptionWhenWorkFlowRunJobsReceivesInvalidResponseTest() throws GenericAdapterException {
        stubFor(
                get(urlEqualTo(
                        "/repos/github-insights/github-metrics/actions/runs/8828175949/jobs"
                )).willReturn(
                        aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody("")
                )
        );
        assertThrows(
                UnableToParseGithubResponseException.class,
                () -> workFlowRunJobsAdapter.getWorkFlowRunJobs(
                        "github-metrics",
                        8828175949L)
        );
    }
}
