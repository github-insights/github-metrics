package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.exceptions.GenericAdapterException;
import be.xplore.githubmetrics.githubadapter.config.GithubConfig;
import be.xplore.githubmetrics.githubadapter.exceptions.UnableToParseGHActionRunsException;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkflowRunsAdapterTest {

    private WireMockServer wireMockServer;
    private WorkFlowRunsAdapter workflowRunsAdapter;

    @BeforeEach
    void setupWireMock() {
        this.wireMockServer = new WireMockServer(
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
        workflowRunsAdapter = new WorkFlowRunsAdapter(
                new GithubAdapter(githubConfig)
        );
        configureFor("localhost", wireMockServer.port());
    }

    @AfterEach
    void stopWireMock() {
        wireMockServer.stop();
    }

    @Test
    void workFlowRunsTest() throws GenericAdapterException {
        stubFor(
                get(urlEqualTo(
                        "/repos/github-insights/github-metrics/actions/runs?created=%3E%3D" + LocalDate.now().minusDays(1)
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
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
    }

    @Test
    void workFlowRunsInvalidResponseTest() throws GenericAdapterException {
        stubFor(get(urlEqualTo("/repos/github-insights/github-metrics/actions/runs?created=%3E%3D" + LocalDate.now().minusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
                .willReturn(
                        aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody("")
                )
        );
        assertThrows(
                UnableToParseGHActionRunsException.class,
                () -> workflowRunsAdapter.getLastDaysWorkflows("github-metrics")
        );
    }

}
