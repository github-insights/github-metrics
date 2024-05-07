package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.exceptions.GenericAdapterException;
import be.xplore.githubmetrics.domain.job.model.Job;
import be.xplore.githubmetrics.domain.repository.Repository;
import be.xplore.githubmetrics.domain.workflowrun.model.WorkflowRun;
import be.xplore.githubmetrics.domain.workflowrun.model.WorkflowRunStatus;
import be.xplore.githubmetrics.githubadapter.config.GithubApiAuthorization;
import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import be.xplore.githubmetrics.githubadapter.config.GithubRestClientConfig;
import be.xplore.githubmetrics.githubadapter.exceptions.UnableToParseGithubResponseException;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JobsAdapterTest {
    private final WorkflowRun workflowRun = new WorkflowRun(
            8828175949L,
            "Workflow Run",
            WorkflowRunStatus.DONE,
            new Repository(
                    8828175049L,
                    "github-metrics",
                    "github-metrics",
                    new ArrayList<String>()
            ));
    private WireMockServer wireMockServer;
    private JobsAdapter jobsAdapter;

    @BeforeEach
    void setupWireMock() {
        wireMockServer = new WireMockServer(
                wireMockConfig().dynamicPort()
        );

        wireMockServer.start();
        GithubProperties githubProperties = new GithubProperties(
                "http",
                "localhost",
                String.valueOf(wireMockServer.port()),
                "github-insights",
                new GithubProperties.Application(
                        "123",
                        "123456",
                        "pem-key"
                )
        );
        RestClient restClient = new GithubRestClientConfig().getGithubRestClient(githubProperties);
        GithubApiAuthorization mockGithubApiAuthorization = Mockito.mock(GithubApiAuthorization.class);
        Mockito.when(mockGithubApiAuthorization.getAuthHeader()).thenReturn(httpHeaders -> {
            httpHeaders.setBearerAuth("token");
        });
        jobsAdapter = new JobsAdapter(
                new GithubAdapter(
                        restClient,
                        githubProperties,
                        mockGithubApiAuthorization
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

        List<Job> workFlowRunJobs = jobsAdapter
                .getAllJobsForWorkflowRun(this.workflowRun);
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
                () -> jobsAdapter.getAllJobsForWorkflowRun(
                        this.workflowRun
                )
        );
    }
}
