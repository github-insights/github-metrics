package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.githubadapter.config.GithubApiAuthorization;
import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import be.xplore.githubmetrics.githubadapter.config.GithubRestClientConfig;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestClient;

import java.util.HashMap;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GithubAdapterTest {

    private GithubAdapter githubAdapter;
    private WireMockServer wireMockServer;

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
        githubAdapter = new GithubAdapter(
                restClient,
                githubProperties,
                mockGithubApiAuthorization);

        configureFor(wireMockServer.port());
    }

    @AfterEach
    void tearDown() {
        this.wireMockServer.stop();
    }

    @Test
    void getResponseSpecContainsCorrectBody() {
        final var expected_body = "test body";

        stubFor(get("/test/url")
                .willReturn(ok().withBody(expected_body))
        );
        var actual_body = this.githubAdapter
                .getResponseSpec("test/url", new HashMap<>())
                .body(String.class);
        assertEquals(expected_body, actual_body);
    }

    @Test
    void getResponseSpecShouldWorkWithLeadingSlash() {
        final var expected_body = "test body";

        stubFor(get("/test/url")
                .willReturn(ok().withBody(expected_body))
        );
        var actual_body = this.githubAdapter
                .getResponseSpec("/test/url", new HashMap<>())
                .body(String.class);
        assertEquals(expected_body, actual_body);
    }

    @Test
    void getResponseSpecShouldAddMapAsParameters() {
        final var expected_body = "test body";
        final var key = "param";
        final var value = "value";

        stubFor(get("/test/url?param=value")
                .willReturn(ok().withBody(expected_body))
        );
        var queryParams = new HashMap<String, String>();
        queryParams.put(key, value);

        var actual_body = this.githubAdapter
                .getResponseSpec("test/url", queryParams)
                .body(String.class);
        assertEquals(expected_body, actual_body);
    }

    @Test
    void getResponseSpecShouldWorkWithFullUrl() {
        final var expected_body = "test body";

        stubFor(get("/test/url")
                .willReturn(ok().withBody(expected_body))
        );
        var actual_body = this.githubAdapter
                .getResponseSpec("http://localhost:" + wireMockServer.port() + "/test/url")
                .body(String.class);
        assertEquals(expected_body, actual_body);
    }

    @Test
    void getResponseSpecShouldWorkWithFullUrlWithParameters() {
        final var expected_body = "test body";

        stubFor(get("/test/url?param=value")
                .willReturn(ok().withBody(expected_body))
        );
        var actual_body = this.githubAdapter
                .getResponseSpec("http://localhost:" + wireMockServer.port() + "/test/url?param=value")
                .body(String.class);
        assertEquals(expected_body, actual_body);
    }
}

