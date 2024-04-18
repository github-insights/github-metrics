package be.xplore.githubmetrics.githubAdapter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class githubAdapterTest {
    @Test
    public void testGithub() {
        assertEquals(
                GithubAdapterHello.hello(),
                "GithubAdapter"
        );
    }

}
