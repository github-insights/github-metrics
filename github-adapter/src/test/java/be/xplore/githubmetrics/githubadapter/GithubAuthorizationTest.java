package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.githubadapter.config.GithubApiAuthorization;
import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import be.xplore.githubmetrics.githubadapter.config.GithubRestClientConfig;
import be.xplore.githubmetrics.githubadapter.exceptions.UnableToAuthenticateGithubAppException;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GithubAuthorizationTest {

    private final GithubProperties.Application githubConfigApplication = new GithubProperties.Application(
            "123",
            "123456",
            "-----BEGIN PRIVATE KEY-----\n" +
                    "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCjknN/LTKuPvXU\n" +
                    "bvLvyAwIs7I189CC+JErIOWwYzE1zRRLn7zNbG+UQJFqVorWm/84OqX/iC9HYudo\n" +
                    "r4qFfT67Fynfs5/OMzbqp7ECX24oKF1G919bUHY8KrJY4CUJhgo4FOda3PuzbmEd\n" +
                    "/POB6OcQ2vyJg1SsGY5REfheIaZ+AHVlTaze6HvejNJWE29iVIBxd0a4cWy9T4xt\n" +
                    "DS5+Z9juoN+l8QvPjFaYenPzWgOqyw5Fsgkulv++M4A/htPX3iFWQNV1Mix9Y/aS\n" +
                    "jbi8ft5+rlt6+llVT87fSgdeT9ZCmceIjwtZW+Zk6dlknXRoTI5q6PZfAFVDKhFN\n" +
                    "RKdbSP03AgMBAAECggEAE2aCVlopAausAocqPzBN5RZTE70YRQBwT1o2g+Yv5v6s\n" +
                    "4o9OmFq9HStPU/pxuySDb8rc92LSoLflVHBFkLGbKBuGNucaFB3U7J35C5v/97lZ\n" +
                    "3tnmMHFppJc30fy7x9ZjDeXEzu0Y2V5FHIZs50KVbVVb0H+IImVhkNH90ERTd+wr\n" +
                    "dbQ2lVg+lv86depJ5YhKs7iDbFnztx+OAdHrAo6qEff1gODv+9JK172pBmZsb+j6\n" +
                    "l/8FSWJN57FfV61QuaKWezS3ASvxDWsOlI7QLuQwOzg4J4LioBsdE+Q3uHhkx6oF\n" +
                    "f/hyYkexsnAl933C/VETK/9tjQf6nBHa29i1nq9k+QKBgQDM/EXj8UR7CJ9cINY4\n" +
                    "QLuMDT4qPgIPjVccWJsv/roL43SYZ1NSs7FcfnRWQgCEtAClMXY8E6Q0zMiRRTu0\n" +
                    "eQkHJabC0R8HZbF3AKj7zMwTj9X6qm4mondXsqJ0k+YqDlEz2fHXbABHAodL1nlu\n" +
                    "wC52I+Nk+ZmLTBsVd1SFgnM8kwKBgQDMR7YheIf6Z8zwuDpbM09fCHXe5SnWSP3m\n" +
                    "kN/WGvnTelvi1SUv8cn1+9bc17yJEF35M4i6Fracox+LlGfDJ8neKDJ0MlqKH7Jd\n" +
                    "244YiXUaMWQbZcXLx052vwF/Vi7gVnFxdqCM7Nx/AV1vTN+Z7OH5JuQOM+qazR4a\n" +
                    "YDYaSsFHTQKBgDtv7ugQXlX6gxLYpqT7CCas9FiVUE2oIxkiDCWXi+TEmFtUopF5\n" +
                    "bzUtqZgVXUcdVo6P0APNgjCZLJMK6ywCaH69CSS2NHQVpaam91jD4mzNqTMc1gG3\n" +
                    "3Dj+oCKDfBq3ug355SkctNviPM7dqqpVaWNyNo5h3YbJk5Te3BA2aimnAoGAEdqy\n" +
                    "sHo4aEpqPx/a+d2iMkwrATBGV9RJXL2M0snIzBMFtO2sMmSPolBAl0zDzbcAf6dh\n" +
                    "a+JQU6BuQWTXLNdtbV1WC5HbF/dtP4bRBJP/CCsI9NwQTZ893GMVXmvJ7RGhGKml\n" +
                    "nquVGgSkhfXSFUH+/ifIBvXCq4UB/IwsLmAaRIECgYAeX/Lc1r2TWySP0ny+Ra+H\n" +
                    "fUUh3gRV/FFEnVfTiyIcP3UL4ikPOCXrDaIE8QOPs5hUzzen5Vo2NG66LyzgH2HG\n" +
                    "NAsWO4FEfeuh1jOn9Nz+FIvcATdB2GFYT7qRb44gNq9yWj7Dx9BFByLXht4XM1cH\n" +
                    "n3lLUx3lpr32I+zijs25AQ==\n" +
                    "-----END PRIVATE KEY-----"
    );
    private GithubProperties githubProperties;
    private WireMockServer wireMockServer;

    @BeforeEach
    void setupWireMock() {
        wireMockServer = new WireMockServer(
                wireMockConfig().dynamicPort()
        );
        wireMockServer.start();
        configureFor("localhost", wireMockServer.port());
        githubProperties = new GithubProperties(
                "http",
                "localhost",
                String.valueOf(wireMockServer.port()),
                "github-insights",
                githubConfigApplication
        );
    }

    @Test
    void TestGithubAuthorizationGetAuthHeaderThrowsException() {
        githubProperties = new GithubProperties(
                "http",
                "localhost",
                String.valueOf(wireMockServer.port()),
                "github-insights",
                new GithubProperties.Application(
                        "123",
                        "234",
                        "pem-key"
                )
        );
        GithubApiAuthorization githubApiAuthorization =
                new GithubApiAuthorization(
                        githubProperties,
                        new GithubRestClientConfig().getGithubRestClient(githubProperties)
                );

        assertThrows(
                UnableToAuthenticateGithubAppException.class,
                githubApiAuthorization::getAuthHeader
        );
    }

    @Test
    void testGithubAuthorizationGetAuthHeaderBadBodyThrowException() {
        GithubApiAuthorization githubApiAuthorization =
                new GithubApiAuthorization(
                        githubProperties,
                        new GithubRestClientConfig().getGithubRestClient(githubProperties)
                );

        stubFor(
                post(urlEqualTo(
                        "/app/installations/" + githubProperties.application().installId() + "/access_tokens"
                )).willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("")));

        assertThrows(
                UnableToAuthenticateGithubAppException.class,
                githubApiAuthorization::getAuthHeader
        );

    }

    @Test
    void testGithubAuthorizationGetAuthHeaderReturnsHeader() {
        GithubApiAuthorization githubApiAuthorization =
                new GithubApiAuthorization(
                        githubProperties,
                        new GithubRestClientConfig().getGithubRestClient(githubProperties)
                );

        stubFor(
                post(urlEqualTo(
                        "/app/installations/" + githubProperties.application().installId() + "/access_tokens"
                )).willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("GithubAuthorizationResponse.json")));

        assertInstanceOf(Consumer.class, githubApiAuthorization.getAuthHeader());

    }
}
