package be.xplore.githubmetrics;

import be.xplore.githubmetrics.prometheusexporter.job.JobsLabelCountsOfLastDayExporter;
import be.xplore.githubmetrics.prometheusexporter.workflowrun.WorkflowRunStatusCountsOfLastDayExporter;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(
        webEnvironment = RANDOM_PORT
)
class IntegrationTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationTests.class);
    private static WireMockServer wireMockServer;

    @Autowired
    private WorkflowRunStatusCountsOfLastDayExporter workflowRunStatusCountsOfLastDayExporter;
    @Autowired
    private JobsLabelCountsOfLastDayExporter jobsLabelCountsOfLastDayExporter;
    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void beforeAll() {
        wireMockServer = TestUtility.getWireMockServer();
        System.setProperty("APP_GITHUB_URL", "http://localhost:" + wireMockServer.port());
    }

    @AfterAll
    static void afterAll() {
        wireMockServer.stop();
    }

    @BeforeEach
    void setUp() {
        stubFor(WireMock.post("/app/installations/50174772/access_tokens").willReturn(ok()
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("GithubAuthorizationResponse.json")));

        stubFor(WireMock.get("/orgs/github-insights/repos?per_page=100")
                .willReturn(ok()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("GithubMetricsRepositoryTestData.json")));

        stubFor(WireMock.get("/repos/github-insights/github-metrics/actions/runs?created=%3E%3D"
                                + TestUtility.yesterday()
                        )
                        .willReturn(ok()
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .withBodyFile("WorkFlowRunsValidTestData.json")
                        )
        );
        stubFor(WireMock.get("/repos/github-insights/github-metrics/actions/runs/8784314559/jobs")
                .willReturn(ok()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("JobsValidTestData.json")));
        stubFor(WireMock.get("/repos/github-insights/github-metrics/actions/runs/8784267977/jobs")
                .willReturn(ok()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("JobsValidTestData.json")));
    }

    @Test
    void retrieveAndExportJobsShouldCorrectlyDisplayOnActuatorEndpoint() throws Exception {
        this.jobsLabelCountsOfLastDayExporter.run();

        mockMvc.perform(MockMvcRequestBuilders
                .get("/actuator/prometheus")
        ).andExpect(
                content().string(Matchers.containsString("workflow_run_jobs{conclusion=\"SUCCESS\",status=\"DONE\",} 2.0"))
        );
    }

    @Test
    void retrieveAndExportWorkflowRunsShouldCorrectlyDisplayOnActuatorEndpoint() throws Exception {
        this.workflowRunStatusCountsOfLastDayExporter.run();

        mockMvc.perform(MockMvcRequestBuilders
                .get("/actuator/prometheus")
        ).andExpect(
                content().string(Matchers.containsString("workflow_runs{status=\"DONE\",} 2.0"))
        );
    }
}
