package be.xplore.githubmetrics;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class TestUtility {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestUtility.class);

    public static String yesterday() {
        return LocalDate.now().minusDays(1)
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
}
