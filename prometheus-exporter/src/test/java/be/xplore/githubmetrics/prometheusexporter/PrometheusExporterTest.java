package be.xplore.githubmetrics.prometheusexporter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PrometheusExporterTest {
    @Test
    public void testPrometheusExporter() {
        Assertions.assertEquals(
                PrometheusExporterHello.hello(),
                "PrometheusExporter"
        );
    }

}
