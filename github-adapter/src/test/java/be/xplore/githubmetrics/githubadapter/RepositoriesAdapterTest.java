package be.xplore.githubmetrics.githubadapter;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RepositoriesAdapterTest {

    private WireMockServer wireMockServer;
    private RepositoriesAdapter repositoriesAdapter;

    @BeforeEach
    void setUp() throws IOException {
        wireMockServer = new WireMockServer(
                wireMockConfig().dynamicPort()
        );

        wireMockServer.start();
        var githubProperties = TestUtility.getNoAuthGithubProperties(wireMockServer.port());
        var restClient = TestUtility.getDefaultRestClientNoAuth(githubProperties);

        repositoriesAdapter = new RepositoriesAdapter(
                githubProperties,
                restClient
        );

        configureFor("localhost", wireMockServer.port());
    }

    @AfterEach
    void tearDown() {
        this.wireMockServer.stop();
    }

    @Test
    void getAllRepositoriesShouldReturnListOfRepositories() {
        stubFor(get("/orgs/github-insights/repos?per_page=100")
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("100RepositoriesTestData.json")));

        var list = this.repositoriesAdapter.getAllRepositories();
        assertEquals(100, list.size());
    }

    @Test
    void getAllRepositoriesShouldFollowLinkHeader() {
        stubFor(get("/orgs/github-insights/repos?per_page=100")
                .willReturn(ok()
                        .withHeader(
                                "link",
                                "<http://localhost:" + wireMockServer.port() + "/orgs/github-insights/repos?since=369>; " +
                                        "rel=\"next\", <https://api.github.com/repositories{?since}>; rel=\"first\""
                        )
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("2RepositoriesTestData.json")));

        stubFor(get("/orgs/github-insights/repos?since=369")
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("2RepositoriesTestData.json")));

        var list = this.repositoriesAdapter.getAllRepositories();
        assertEquals(4, list.size());
    }

    @Test
    void getAllRepositoriesShouldThrowExceptionOnInvalidResponseBody() {
        stubFor(get("/orgs/github-insights/repos?per_page=100")
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody("invalid body")));

        assertThrows(
                RestClientException.class,
                this.repositoriesAdapter::getAllRepositories
        );
    }

    @Test
    void getAllRepositoriesStopsRecursionWhenRelNextNotPresent() {
        stubFor(get("/orgs/github-insights/repos?per_page=100")
                .willReturn(ok()
                        .withHeader(
                                "link",
                                "<http://localhost:" + wireMockServer.port() + "/orgs/github-insights/repos?since=1>; " +
                                        "rel=\"next\", <https://api.github.com/repositories{?since}>; rel=\"first\""
                        )
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("2RepositoriesTestData.json")));
        stubFor(get("/orgs/github-insights/repos?since=1")
                .willReturn(ok()
                        .withHeader(
                                "link",
                                "<http://localhost:" + wireMockServer.port() + "/orgs/github-insights/repos?since=2>; " +
                                        "rel=\"prev\", <https://api.github.com/repositories{?since}>; rel=\"first\""
                        )
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("2RepositoriesTestData.json")));
        var list = this.repositoriesAdapter.getAllRepositories();
        assertEquals(4, list.size());

    }
}
