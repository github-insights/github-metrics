package be.xplore.githubmetrics;

import be.xplore.githubmetrics.prometheusexporter.apistate.ApiRateLimitStateExporter;
import be.xplore.githubmetrics.prometheusexporter.job.JobsLabelCountsOfLastDayExporter;
import be.xplore.githubmetrics.prometheusexporter.pullrequest.PullRequestExporter;
import be.xplore.githubmetrics.prometheusexporter.repository.RepositoryCountExporter;
import be.xplore.githubmetrics.prometheusexporter.selfhostedrunner.SelfHostedRunnerCountsExporter;
import be.xplore.githubmetrics.prometheusexporter.workflowrun.ActiveWorkflowRunStatusCountsExporter;
import be.xplore.githubmetrics.prometheusexporter.workflowrun.WorkflowRunBuildTimesOfLastDayExporter;
import be.xplore.githubmetrics.prometheusexporter.workflowrun.WorkflowRunStatusCountsOfLastDayExporter;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(
        webEnvironment = RANDOM_PORT
)
class IntegrationTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationTests.class);
    private static final String ACTUATOR_ENDPOINT = "/actuator/prometheus";
    private static WireMockServer wireMockServer;
    @Value("classpath:__files/PullRequestsValidData.json")
    private Resource pullRequestJson;
    @Autowired
    private RepositoryCountExporter repositoryCountExporter;
    @Autowired
    private WorkflowRunStatusCountsOfLastDayExporter workflowRunStatusCountsOfLastDayExporter;
    @Autowired
    private WorkflowRunBuildTimesOfLastDayExporter workflowRunBuildTimesOfLastDayExporter;
    @Autowired
    private JobsLabelCountsOfLastDayExporter jobsLabelCountsOfLastDayExporter;
    @Autowired
    private PullRequestExporter pullRequestExporter;
    @Autowired
    private SelfHostedRunnerCountsExporter selfHostedRunnerCountsExporter;
    @Autowired
    private ApiRateLimitStateExporter apiRateLimitStateExporter;

    @Autowired
    private ActiveWorkflowRunStatusCountsExporter activeWorkflowRunStatusCountsExporter;
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
        stubFor(post("/app/installations/50174772/access_tokens").willReturn(ok()
                .withHeaders(TestUtility.getRateLimitingHeaders())
                .withBodyFile("GithubAuthorizationResponse.json")));

        stubFor(get("/installation/repositories?per_page=100&page=0")
                .willReturn(ok()
                        .withHeaders(TestUtility.getRateLimitingHeaders())
                        .withBodyFile("GithubMetricsRepositoryTestData.json")));

        this.stubForWorkflowRunEndpoints();
        this.stubForJobsEndpoints();
        this.stubForBuildTimeEndpoints();
        this.stubForPullRequestsEndpoints();
        this.stubForSelfHostedRunnersEndpoints();
    }

    private void stubForWorkflowRunEndpoints() {
        stubFor(get("/repos/github-insights/github-metrics/actions/runs?per_page=100&created=%3E%3D"
                        + TestUtility.yesterday() + "&page=0"
                )
                        .willReturn(ok()
                                .withHeaders(TestUtility.getRateLimitingHeaders())
                                .withBodyFile("WorkFlowRunsValidTestData.json")
                        )
        );
        stubFor(get("/repos/github-insights/github-metrics/actions/runs?per_page=100")
                .willReturn(ok()
                        .withHeaders(TestUtility.getRateLimitingHeaders())
                        .withBodyFile("ActiveWorkFlowRunsValidTestData.json")
                )
        );
    }

    private void stubForJobsEndpoints() {
        stubFor(get("/repos/github-insights/github-metrics/actions/runs/8784314559/jobs?per_page=100&page=0")
                .willReturn(ok()
                        .withHeaders(TestUtility.getRateLimitingHeaders())
                        .withBodyFile("JobsValidTestData.json")));
        stubFor(get("/repos/github-insights/github-metrics/actions/runs/8784267977/jobs?per_page=100&page=0")
                .willReturn(ok()
                        .withHeaders(TestUtility.getRateLimitingHeaders())
                        .withBodyFile("JobsValidTestData.json")));
    }

    private void stubForBuildTimeEndpoints() {
        stubFor(get("/repos/github-insights/github-metrics/actions/runs/8784314559/timing")
                .willReturn(ok()
                        .withHeaders(TestUtility.getRateLimitingHeaders())
                        .withBodyFile("WorkflowRunBuildTime.json")));
        stubFor(get("/repos/github-insights/github-metrics/actions/runs/8784267977/timing")
                .willReturn(ok()
                        .withHeaders(TestUtility.getRateLimitingHeaders())
                        .withBodyFile("WorkflowRunBuildTime.json")));
    }

    private void stubForPullRequestsEndpoints() {
        String json = TestUtility.asString(pullRequestJson);
        json = json.replace("{{todays_date}}", TestUtility.getDateTimeXDaysAgo(0));
        json = json.replace("{{yesterdays_date}}", TestUtility.getDateTimeXDaysAgo(1));
        json = json.replace("{{3_days_ago_date}}", TestUtility.getDateTimeXDaysAgo(3));
        json = json.replace("{{8_days_ago_date}}", TestUtility.getDateTimeXDaysAgo(8));
        json = json.replace("{{16_days_ago_date}}", TestUtility.getDateTimeXDaysAgo(16));

        stubFor(
                get(urlEqualTo(
                        "/repos/github-insights/github-metrics/pulls?per_page=100&state=all&page=0"
                )).willReturn(
                        aResponse()
                                .withHeaders(TestUtility.getRateLimitingHeaders())
                                .withBody(json)));
    }

    private void stubForSelfHostedRunnersEndpoints() {
        stubFor(
                get(urlEqualTo(
                        "/orgs/github-insights/actions/runners?per_page=100&page=0"
                )).willReturn(
                        aResponse()
                                .withHeaders(TestUtility.getRateLimitingHeaders())
                                .withBodyFile("SelfHostedRunnersMacData.json")));
        stubFor(
                get(urlEqualTo(
                        "/repos/github-insights/github-metrics/actions/runners?per_page=100&page=0"
                )).willReturn(
                        aResponse()
                                .withHeaders(TestUtility.getRateLimitingHeaders())
                                .withBodyFile("SelfHostedRunnersOtherData.json")));
    }

    @Test
    void retrieveAndExportRepositoriesShouldCorrectlyDisplayOnActuatorEndpoint() throws Exception {
        this.repositoryCountExporter.run();
        mockMvc.perform(MockMvcRequestBuilders
                .get(ACTUATOR_ENDPOINT)
        ).andExpect(containsStr("repositories_count{organization=\"github-insights\"} 1.0"));
    }

    @Test
    void retrieveAndExportPullRequestsShouldCorrectlyDisplayOnActuatorEndpoint() throws Exception {
        this.pullRequestExporter.run();

        mockMvc.perform(MockMvcRequestBuilders
                .get(ACTUATOR_ENDPOINT)
        ).andExpect(containsStr("pull_requests_count_of_last_2_days{organization=\"github-insights\",state=\"OPEN\"} 1.0"));
    }

    @Test
    void retrieveAndExportJobsShouldCorrectlyDisplayOnActuatorEndpoint() throws Exception {
        this.jobsLabelCountsOfLastDayExporter.run();

        mockMvc.perform(MockMvcRequestBuilders
                .get(ACTUATOR_ENDPOINT)
        ).andExpect(containsStr("workflow_run_jobs{conclusion=\"SUCCESS\",organization=\"github-insights\",status=\"DONE\"} 2.0"));
    }

    @Test
    void retrieveAndExportWorkflowRunsShouldCorrectlyDisplayOnActuatorEndpoint() throws Exception {
        this.workflowRunStatusCountsOfLastDayExporter.run();

        mockMvc.perform(MockMvcRequestBuilders
                .get(ACTUATOR_ENDPOINT)
        ).andExpect(
                content().string(Matchers.containsString("workflow_runs{organization=\"github-insights\",status=\"DONE\"} 2.0"))
        );
    }

    @Test
    void retrieveAndExportActiveWorkflowRunsShouldCorrectlyDisplayOnActuatorEndpoint() throws Exception {
        this.activeWorkflowRunStatusCountsExporter.run();

        mockMvc.perform(MockMvcRequestBuilders
                .get(ACTUATOR_ENDPOINT)
        ).andExpect(containsStr("active_workflow_runs{organization=\"github-insights\",status=\"IN_PROGRESS\"} 1.0"));
        mockMvc.perform(MockMvcRequestBuilders
                .get(ACTUATOR_ENDPOINT)
        ).andExpect(containsStr("active_workflow_runs{organization=\"github-insights\",status=\"PENDING\"} 1.0"));
    }

    @Test
    void retrieveAndExportWorkflowRunBuildTimesShouldCorrectlyDisplayOnActuatorEndpoint() throws Exception {
        this.workflowRunBuildTimesOfLastDayExporter.run();

        mockMvc.perform(MockMvcRequestBuilders
                .get(ACTUATOR_ENDPOINT)
        ).andExpect(containsStr(
                "workflow_runs_total_build_times{organization=\"github-insights\",status=\"DONE\"} 272000.0"
        ));
        mockMvc.perform(MockMvcRequestBuilders
                .get(ACTUATOR_ENDPOINT)
        ).andExpect(containsStr(
                "workflow_runs_average_build_times{organization=\"github-insights\",status=\"DONE\"} 136000.0"
        ));
    }

    @Test
    void retrieveAndExportSelfHostedRunnersShouldCorrectlyDisplayStatusesAndOss() throws Exception {
        this.selfHostedRunnerCountsExporter.run();
        mockMvc.perform(MockMvcRequestBuilders
                .get(ACTUATOR_ENDPOINT)
        ).andExpect(containsStr("self_hosted_runners{organization=\"github-insights\",os=\"LINUX\",status=\"BUSY\"} 1.0"));
        mockMvc.perform(MockMvcRequestBuilders
                .get(ACTUATOR_ENDPOINT)
        ).andExpect(containsStr("self_hosted_runners{organization=\"github-insights\",os=\"WINDOWS\",status=\"OFFLINE\"} 1.0"));
        mockMvc.perform(MockMvcRequestBuilders
                .get(ACTUATOR_ENDPOINT)
        ).andExpect(containsStr("self_hosted_runners{organization=\"github-insights\",os=\"MAC_OS\",status=\"IDLE\"} 1.0"));
    }

    @Test
    void retrieveAndExportApiRateLimitStateShouldCorrectlyExportData() throws Exception {
        this.apiRateLimitStateExporter.run();
        mockMvc.perform(MockMvcRequestBuilders
                        .get(ACTUATOR_ENDPOINT)
                )
                .andExpect(containsStr("api_ratelimit_state_actual_req{organization=\"github-insights\"} 0.0"))
                .andExpect(containsStr("api_ratelimit_state_ideal_req{organization=\"github-insights\"} 0.0"))
                .andExpect(containsStr("api_ratelimit_state_limit{organization=\"github-insights\"} 4500.0"))
                .andExpect(containsStr("api_ratelimit_state_paused{organization=\"github-insights\"} 0.0"))
                .andExpect(containsStr("api_ratelimit_state_remaining{organization=\"github-insights\"} 4500.0"))
                .andExpect(containsStr("api_ratelimit_state_reset{organization=\"github-insights\"}"))
                .andExpect(containsStr("api_ratelimit_state_status{organization=\"github-insights\"} 4.0"))
                .andExpect(containsStr("api_ratelimit_state_used{organization=\"github-insights\"} 0.0"));

    }

    private ResultMatcher containsStr(String string) {
        return content().string(Matchers.containsString(string));
    }
}
