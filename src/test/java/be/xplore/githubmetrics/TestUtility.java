package be.xplore.githubmetrics;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.nio.charset.StandardCharsets.UTF_8;

public class TestUtility {

    public static final String GITHUB_JSON = "application/vnd.github+json";
    private static final Logger LOGGER = LoggerFactory.getLogger(TestUtility.class);

    public static HttpHeaders getRateLimitingHeaders() {
        return new HttpHeaders(
                HttpHeader.httpHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, GITHUB_JSON),
                HttpHeader.httpHeader("x-ratelimit-limit", "5000"),
                HttpHeader.httpHeader("x-ratelimit-remaining", "5000"),
                HttpHeader.httpHeader("x-ratelimit-reset", Long.toString(Instant.now().toEpochMilli())),
                HttpHeader.httpHeader("x-ratelimit-used", "0")
        );
    }

    public static String yesterday() {
        return LocalDate.now().minusDays(1)
                .format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public static String getDateTimeXDaysAgo(int x) {
        return ZonedDateTime.now().minusDays(x).plusHours(2).toInstant().toString();
    }

    public static WireMockServer getWireMockServer() {
        var wireMockServer = new WireMockServer(
                wireMockConfig().dynamicPort()
        );
        wireMockServer.start();
        configureFor("localhost", wireMockServer.port());
        LOGGER.info(
                "Wiremock Server running on {}.",
                wireMockServer.baseUrl()
        );
        return wireMockServer;
    }

    public static String asString(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
