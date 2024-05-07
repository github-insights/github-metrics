package be.xplore.githubmetrics.githubadapter;

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
    void setUp() {
        wireMockServer = new WireMockServer(
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
        repositoriesAdapter = new RepositoriesAdapter(
                githubProperties,
                new GithubAdapter(
                        restClient,
                        githubProperties,
                        mockGithubApiAuthorization
                ));

        configureFor("localhost", wireMockServer.port());
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

    @Test
    void getAllRepositoriesShouldFollowLinkHeader() {
        stubFor(get("/orgs/github-insights/repos")
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
        stubFor(get("/orgs/github-insights/repos")
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody("invalid body")));

        assertThrows(
                UnableToParseGithubResponseException.class,
                this.repositoriesAdapter::getAllRepositories
        );
    }

    @Test
    void getAllRepositoriesStopsRecursionWhenRelNextNotPresent() {
        stubFor(get("/orgs/github-insights/repos")
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
