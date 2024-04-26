package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.githubadapter.config.GithubConfig;
import be.xplore.githubmetrics.githubadapter.exceptions.UnableToParseGHRepositoryArrayException;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RepositoriesAdapterTest {

    private final RepositoriesAdapter repositoriesAdapter;
    private WireMockServer wireMockServer;

    RepositoriesAdapterTest() {
        var config = new GithubConfig(
                "http", "localhost", "8081", "",
                "github-insights"
        );
        this.repositoriesAdapter = new RepositoriesAdapter(
                config, new GithubAdapter(config)
        );
    }

    @BeforeEach
    void setUp() {
        this.wireMockServer = new WireMockServer(8081);
        this.wireMockServer.start();
        configureFor(8081);
    }

    @AfterEach
    void tearDown() {
        this.wireMockServer.stop();
    }

    @Test
    void getAllRepositoriesShouldReturnListOfRepositories() {
        stubFor(get("/orgs/github-insights/repos")
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("100RepositoriesTestData.json")));

        var list = this.repositoriesAdapter.getAllRepositories();
        assertEquals(100, list.size());
    }

    //
    @Test
    void getAllRepositoriesShouldFollowLinkHeader() {
        stubFor(get("/orgs/github-insights/repos")
                .willReturn(ok()
                        .withHeader(
                                "link",
                                "<http://localhost:8081/orgs/github-insights/repos?since=369>; rel=\"next\", <https://api.github.com/repositories{?since}>; rel=\"first\""
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
        stubFor(get("/orgs/github-insights/repos")
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody("invalid body")));

        assertThrows(
                UnableToParseGHRepositoryArrayException.class,
                this.repositoriesAdapter::getAllRepositories
        );
    }

    @Test
    void getAllRepositoriesStopsRecursionWhenRelNextNotPresent() {
        stubFor(get("/orgs/github-insights/repos")
                .willReturn(ok()
                        .withHeader(
                                "link",
                                "<http://localhost:8081/orgs/github-insights/repos?since=1>; rel=\"next\", <https://api.github.com/repositories{?since}>; rel=\"first\""
                        )
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("2RepositoriesTestData.json")));
        stubFor(get("/orgs/github-insights/repos?since=1")
                .willReturn(ok()
                        .withHeader(
                                "link",
                                "<http://localhost:8081/orgs/github-insights/repos?since=2>; rel=\"prev\", <https://api.github.com/repositories{?since}>; rel=\"first\""
                        )
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("2RepositoriesTestData.json")));
        var list = this.repositoriesAdapter.getAllRepositories();
        assertEquals(4, list.size());

    }
}
