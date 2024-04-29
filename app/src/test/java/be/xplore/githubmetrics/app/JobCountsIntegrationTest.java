package be.xplore.githubmetrics.app;

import be.xplore.githubmetrics.domain.schedulers.ports.JobsUseCase;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@WireMockTest(httpPort = 8081)
@SpringBootTest(
        webEnvironment = RANDOM_PORT
)
@AutoConfigureObservability
public class JobCountsIntegrationTest {

    @Autowired
    private JobsUseCase jobsUseCase;
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        stubFor(WireMock.get("/orgs/github-insights/repos")
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("GithubMetricsRepositoryTestData.json")));

        stubFor(WireMock.get("/repos/github-insights/github-metrics/actions/runs?created=%3E%3D"
                                + LocalDate.now()
                                .minusDays(1)
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        )
                        .willReturn(ok()
                                .withHeader("Content-Type", "application/json")
                                .withBodyFile("WorkFlowRunsValidTestData.json")
                        )
        );
        stubFor(WireMock.get("/repos/github-insights/github-metrics/actions/runs/8784314559/jobs")
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("JobsValidTestData.json")));
        stubFor(WireMock.get("/repos/github-insights/github-metrics/actions/runs/8784267977/jobs")
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("JobsValidTestData.json")));
    }

    @Test
    void retrieveAndExportShouldCorrectlyDisplayOnActuatorEndpoint() throws Exception {
        this.jobsUseCase.retrieveAndExportJobs();

        mockMvc.perform(MockMvcRequestBuilders
                .get("/actuator/prometheus")
        ).andExpect(
                content().string(Matchers.containsString("workflow_run_jobs{conclusion=\"SUCCESS\",status=\"DONE\",} 2.0"))
        );
    }
}
