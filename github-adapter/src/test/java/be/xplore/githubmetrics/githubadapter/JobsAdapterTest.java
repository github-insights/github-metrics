package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.job.Job;
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
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JobsAdapterTest {

    private final WorkflowRun workflowRun = new WorkflowRun(
            8_828_175_949L,
            "Workflow Run",
            WorkflowRunStatus.DONE,
            new Repository(
                    8_828_175_049L,
                    "github-metrics",
                    "github-metrics",
                    new ArrayList<String>()
            ));
    private WireMockServer wireMockServer;
    private JobsAdapter jobsAdapter;

    @BeforeEach
    void setupWireMock() {
        wireMockServer = TestUtility.getWireMockServer();

        var githubProperties = TestUtility.getNoAuthGithubProperties(wireMockServer.port());
        var tokenRestClient = TestUtility.getDefaultRestClientNoAuth(githubProperties);
        jobsAdapter = new JobsAdapter(githubProperties, tokenRestClient);
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
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .withBodyFile("WorkFlowRunJobsValidTestData.json")
                ));

        List<Job> workFlowRunJobs = jobsAdapter
                .getAllJobsForWorkflowRun(this.workflowRun);
        assertEquals(1, workFlowRunJobs.size());
    }

    @Test
    void assertExceptionWhenWorkFlowRunJobsReceivesInvalidResponseTest() {
        stubFor(
                get(urlEqualTo(
                        "/repos/github-insights/github-metrics/actions/runs/8828175949/jobs"
                )).willReturn(
                        aResponse()
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .withBody("")
                )
        );
        assertThrows(
                NullPointerException.class,
                () -> {
                    jobsAdapter.getAllJobsForWorkflowRun(
                            this.workflowRun
                    );
                }
        );
    }
}
