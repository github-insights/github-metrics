package be.xplore.githubmetrics;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.nio.charset.StandardCharsets.UTF_8;

public class TestUtility {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestUtility.class);

    public static String yesterday() {
        return LocalDate.now().minusDays(1)
                .format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public static String getDateXDaysAgo(int x) {
        return LocalDate.now().minusDays(x)
                .format(DateTimeFormatter.ISO_LOCAL_DATE);
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
