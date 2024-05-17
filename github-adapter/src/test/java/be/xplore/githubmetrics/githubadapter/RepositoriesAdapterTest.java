package be.xplore.githubmetrics.githubadapter;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RepositoriesAdapterTest {

    private WireMockServer wireMockServer;
    private RepositoriesAdapter repositoriesAdapter;

    @BeforeEach
    void setUp() {
        wireMockServer = TestUtility.getWireMockServer();
        var githubProperties = TestUtility.getNoAuthGithubProperties(wireMockServer.port());
        var restClient = TestUtility.getDefaultRestClientNoAuth(githubProperties);
        var utilities = new GithubApiUtilities(restClient);

        repositoriesAdapter = new RepositoriesAdapter(
                githubProperties,
                restClient,
                utilities
        );

    }

    @AfterEach
    void tearDown() {
        this.wireMockServer.stop();
    }

    @Test
    void getAllRepositoriesShouldReturnListOfRepositories() {
        stubFor(get("/installation/repositories?per_page=100")
                .willReturn(ok()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("100RepositoriesTestData.json")));

        var list = this.repositoriesAdapter.getAllRepositories();
        assertEquals(100, list.size());
    }

    @Test
    void getAllRepositoriesShouldFollowLinkHeader() {
        stubFor(get("/installation/repositories?per_page=100")
                .willReturn(ok()
                        .withHeader(
                                "link",
                                "<http://localhost:" + wireMockServer.port() + "/installation/repositories?since=369>; " +
                                        "rel=\"next\", <https://api.github.com/repositories{?since}>; rel=\"first\""
                        )
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("2RepositoriesTestData.json")));

        stubFor(get("/installation/repositories?since=369")
                .willReturn(ok()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("2RepositoriesTestData.json")));

        var list = this.repositoriesAdapter.getAllRepositories();
        assertEquals(4, list.size());
    }

    @Test
    void getAllRepositoriesShouldThrowExceptionOnInvalidResponseBody() {
        stubFor(get("/installation/repositories?per_page=100")
                .willReturn(ok()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("invalid body")));

        assertThrows(
                RestClientException.class,
                this.repositoriesAdapter::getAllRepositories
        );
    }

    @Test
    void getAllRepositoriesStopsRecursionWhenRelNextNotPresent() {
        stubFor(get("/installation/repositories?per_page=100")
                .willReturn(ok()
                        .withHeader(
                                "link",
                                "<http://localhost:" + wireMockServer.port() + "/installation/repositories?since=1>; " +
                                        "rel=\"next\", <https://api.github.com/repositories{?since}>; rel=\"first\""
                        )
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("2RepositoriesTestData.json")));
        stubFor(get("/installation/repositories?since=1")
                .willReturn(ok()
                        .withHeader(
                                "link",
                                "<http://localhost:" + wireMockServer.port() + "/installation/repositories?since=2>; " +
                                        "rel=\"prev\", <https://api.github.com/repositories{?since}>; rel=\"first\""
                        )
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("2RepositoriesTestData.json")));
        var list = this.repositoriesAdapter.getAllRepositories();
        assertEquals(4, list.size());

    }
}
