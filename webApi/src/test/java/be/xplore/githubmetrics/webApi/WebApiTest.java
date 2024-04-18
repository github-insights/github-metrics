package be.xplore.githubmetrics.webApi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WebApiTest {
    @Test
    public void testWebApi() {
        Assertions.assertEquals(
                WebApiHello.hello(),
                "WebApi"
        );
    }

}
