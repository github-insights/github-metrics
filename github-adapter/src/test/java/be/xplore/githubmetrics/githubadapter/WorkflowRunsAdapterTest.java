package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.exceptions.GenericAdapterException;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkflowRunsAdapterTest {

    private final Repository repository = new Repository(123L, "github-metrics", "", new ArrayList<>());
    private WireMockServer wireMockServer;
    private WorkflowRunsAdapter workflowRunsAdapter;

    @BeforeEach
    void setupWireMock() {

        this.wireMockServer = new WireMockServer(
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
        workflowRunsAdapter = new WorkflowRunsAdapter(
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
                workflowRunsAdapter.getLastDaysWorkflowRuns(repository)
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
                UnableToParseGithubResponseException.class,
                () -> workflowRunsAdapter.getLastDaysWorkflowRuns(repository)
        );
    }

    @Test
    void workflowRunsQueryShouldHaveCorrectStatus() {
        stubFor(get(urlEqualTo("/repos/github-insights/github-metrics/actions/runs?created=%3E%3D" + LocalDate.now().minusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
                .willReturn(
                        aResponse()
                                .withHeader("Content-Type", "application/json")
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
