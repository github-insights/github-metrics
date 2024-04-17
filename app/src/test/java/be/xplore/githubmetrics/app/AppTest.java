package be.xplore.githubmetrics.app;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppTest {
    @Test
    public void testApp() {
        assertEquals(
                App.hello(),
                "Hello Domain WebApi GithubAdapter"
        );
    }

}
