package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.githubadapter.config.GithubConfig;
import be.xplore.githubmetrics.githubadapter.config.GithubRestClientConfiguration;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.HashMap;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GithubAdapterTest {

    private final GithubConfig githubConfig = new GithubConfig(
            "http",
            "localhost",
            "8081",
            "",
            "github-insights"
    );

    private final RestClient restClient = new GithubRestClientConfiguration().getGithubRestClient(githubConfig);
    private final GithubAdapter githubAdapter = new GithubAdapter(githubConfig, restClient);
    private WireMockServer wireMockServer;

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
                .getResponseSpec("http://localhost:8081/test/url")
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
                .getResponseSpec("http://localhost:8081/test/url?param=value")
                .body(String.class);
        assertEquals(expected_body, actual_body);
    }
}

