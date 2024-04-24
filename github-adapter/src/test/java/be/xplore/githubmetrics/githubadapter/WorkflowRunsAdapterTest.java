package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.exceptions.GenericAdapterException;
import be.xplore.githubmetrics.githubadapter.config.GithubConfig;
import be.xplore.githubmetrics.githubadapter.exceptions.UnableToParseGHActionRunsException;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkflowRunsAdapterTest {

    private final GithubConfig githubConfig = new GithubConfig(
            "http", "localhost", "9090", "",
            "github-insights"
    );
    private final WorkFlowRunsAdapter workflowRunsAdapter = new WorkFlowRunsAdapter(
            new GithubAdapter(githubConfig)
    );

    @Test
    void workFlowRunsTest() throws GenericAdapterException {
        WireMockServer wireMockServer = new WireMockServer(9090);
        wireMockServer.start();
        configureFor("localhost", 9090);
        stubFor(get(urlEqualTo("/repos/github-insights/github-metrics/actions/runs?created=%3E%3D2024-04-10"))
                .willReturn(
                        aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBodyFile("WorkFlowRunsValidTestData.json")
                )
        );
        assertFalse(
                workflowRunsAdapter
                        .getLastDaysWorkflows("github-metrics")
                        .isEmpty()
        );

        wireMockServer.stop();
    }

    @Test
    void workFlowRunsInvalidResponseTest() throws GenericAdapterException {
        WireMockServer wireMockServer = new WireMockServer(9090);
        wireMockServer.start();
        configureFor("localhost", 9090);
        stubFor(get(urlEqualTo("/repos/github-insights/github-metrics/actions/runs?created=%3E%3D2024-04-10"))
                .willReturn(
                        aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody("")//("WorkFlowRunsInvalidTestData.json")
                )
        );
        assertThrows(
                UnableToParseGHActionRunsException.class,
                () -> workflowRunsAdapter.getLastDaysWorkflows("github-metrics")
        );

        wireMockServer.stop();
    }

}
