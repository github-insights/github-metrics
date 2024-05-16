package be.xplore.githubmetrics.prometheusexporter.pullrequest;

import be.xplore.githubmetrics.domain.pullrequest.GetAllPullRequestsUseCase;
import be.xplore.githubmetrics.domain.pullrequest.PullRequest;
import be.xplore.githubmetrics.prometheusexporter.config.SchedulingProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PullRequestExporterTest {
    private PullRequestExporter pullRequestExporter;
    private GetAllPullRequestsUseCase mockUseCase;
    private MeterRegistry registry;

    private List<PullRequest> getPullRequests() {
        List<PullRequest> pullRequests = new ArrayList<>();
        pullRequests.add(new PullRequest(0L, ZonedDateTime.now().minusHours(12)));
        pullRequests.add(new PullRequest(0L, ZonedDateTime.now().minusHours(36)));
        pullRequests.add(new PullRequest(
                0L,
                ZonedDateTime.now().minusHours(36),
                ZonedDateTime.now().minusHours(35),
                null));
        pullRequests.add(new PullRequest(
                0L,
                ZonedDateTime.now().minusHours(72),
                ZonedDateTime.now().minusHours(71),
                ZonedDateTime.now().minusHours(71)));

        return pullRequests;
    }

    @BeforeEach
    void setUp() {
        this.registry = new SimpleMeterRegistry();
        this.mockUseCase = Mockito.mock(GetAllPullRequestsUseCase.class);
        var mockProperties = Mockito.mock(SchedulingProperties.class);
        Mockito.when(mockProperties.pullRequestsInterval()).thenReturn("");

        this.pullRequestExporter = new PullRequestExporter(
                mockUseCase, mockProperties, registry
        );
    }

    @Test
    void pullRequestExporterShouldDisplayCorrectGaugeMetrics() {
        String labelToCheck = "state";
        Mockito.when(this.mockUseCase.getAllPullRequests()).thenReturn(getPullRequests());
        pullRequestExporter.run();
        assertEquals(
                0,
                registry.find("pull_requests_count_of_last_1_days")
                        .tag(labelToCheck, "CLOSED").gauge().value());
        assertEquals(
                1,
                registry.find("pull_requests_count_of_last_1_days")
                        .tag(labelToCheck, "OPEN").gauge().value());
        assertEquals(
                2,
                registry.find("pull_requests_count_of_last_2_days")
                        .tag(labelToCheck, "OPEN").gauge().value());
        assertEquals(
                1,
                registry.find("pull_requests_count_of_last_2_days")
                        .tag(labelToCheck, "CLOSED").gauge().value());
        assertEquals(
                1,
                registry.find("pull_requests_count_of_last_7_days")
                        .tag(labelToCheck, "MERGED").gauge().value());
    }
}
