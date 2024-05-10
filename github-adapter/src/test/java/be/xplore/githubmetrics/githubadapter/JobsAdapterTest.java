package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.exceptions.GenericAdapterException;
import be.xplore.githubmetrics.domain.job.model.Job;
import be.xplore.githubmetrics.domain.repository.Repository;
import be.xplore.githubmetrics.domain.workflowrun.model.WorkflowRun;
import be.xplore.githubmetrics.domain.workflowrun.model.WorkflowRunStatus;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JobsAdapterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobsAdapterTest.class);
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
    void setupWireMock() throws IOException {
        wireMockServer = TestUtility.getWireMockServer();

        var githubProperties = TestUtility.getNoAuthGithubProperties(wireMockServer.port());
        var tokenRestClient = TestUtility.getDefaultRestClientNoAuth(githubProperties);
        jobsAdapter = new JobsAdapter(githubProperties, tokenRestClient);
        LOGGER.info("");
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
                NullPointerException.class,
                () -> {
                    jobsAdapter.getAllJobsForWorkflowRun(
                            this.workflowRun
                    );
                }
        );
    }
}
