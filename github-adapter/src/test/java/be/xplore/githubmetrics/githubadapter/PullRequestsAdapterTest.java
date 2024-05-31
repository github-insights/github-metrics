package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.pullrequest.PullRequest;
import be.xplore.githubmetrics.domain.pullrequest.PullRequestState;
import be.xplore.githubmetrics.domain.repository.Repository;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PullRequestsAdapterTest {
    private final Repository repository = new Repository(
            123L,
            "github-metrics",
            "",
            new ArrayList<>());

    private WireMockServer wireMockServer;
    private PullRequestsAdapter pullRequestsAdapter;

    @BeforeEach
    void setUp() {
        this.wireMockServer = TestUtility.getWireMockServer();
        var githubProperties = TestUtility.getNoAuthGithubProperties(wireMockServer.port());
        var restClient = TestUtility.getDefaultRestClientNoAuth(githubProperties);
        var utilities = new GithubApiUtilities();
        this.pullRequestsAdapter = new PullRequestsAdapter(
                githubProperties, restClient, utilities,
                TestUtility.getCacheEvictionProperties(),
                TestUtility.getApiRateLimitState()
        );
    }

    @AfterEach
    void takeDown() {
        wireMockServer.stop();
    }

    @Test
    void pullRequestQueryWithValidResponseShouldReceiveData() {
        stubFor(
                get(urlEqualTo(
                        "/repos/github-insights/github-metrics/pulls?per_page=100&state=all&page=0"
                )).willReturn(
                        aResponse()
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .withBodyFile("PullRequestsValidData.json")));

        assertEquals(
                5,
                pullRequestsAdapter.getAllPullRequestsForRepository(repository)
                        .size()
        );
    }

    @Test
    void pullRequestWithInvalidDataShouldThrowException() {
        stubFor(
                get(urlEqualTo(
                        "/repos/github-insights/github-metrics/pulls?per_page=100&state=all&page=0"
                )).willReturn(
                        aResponse()
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .withBody("")));

        assertThrows(
                NullPointerException.class,
                () -> pullRequestsAdapter.getAllPullRequestsForRepository(repository)
        );
    }

    @Test
    void pullRequestQueryShouldReturnCorrectStates() {
        stubFor(
                get(urlEqualTo(
                        "/repos/github-insights/github-metrics/pulls?per_page=100&state=all&page=0"
                )).willReturn(
                        aResponse()
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .withBodyFile("PullRequestsValidData.json")));
        List<PullRequest> pullRequests = pullRequestsAdapter.getAllPullRequestsForRepository(repository);
        assertSame(PullRequestState.OPEN, pullRequests.get(0).getState());
        assertSame(PullRequestState.CLOSED, pullRequests.get(1).getState());
        assertSame(PullRequestState.MERGED, pullRequests.get(2).getState());
    }
}
