package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.exceptions.GenericAdapterException;
import be.xplore.githubmetrics.githubadapter.config.GithubConfig;
import be.xplore.githubmetrics.githubadapter.exceptions.InvalidAdapterRequestURIException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertThrows;

class GithubAdapterTest {
    GithubConfig githubConfig = new GithubConfig("http://localhost:9090", "placeholder-token", "github-insights");
    private final GithubAdapter githubAdapter = new GithubAdapter(githubConfig);

    @Test
    void badURITest() throws GenericAdapterException {
        var queryParams = new HashMap<String, String>();
        assertThrows(
                InvalidAdapterRequestURIException.class,
                () -> {
                    githubAdapter.getResponseSpec(
                            "http://localhost:9090/$?^\\$$",
                            queryParams
                    );
                });
    }
}
