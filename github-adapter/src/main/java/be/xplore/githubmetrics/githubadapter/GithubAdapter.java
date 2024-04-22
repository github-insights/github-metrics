package be.xplore.githubmetrics.githubadapter;

import be.xplore.githubmetrics.domain.domain.WorkflowRun;
import be.xplore.githubmetrics.domain.usecases.ports.in.WorkflowRunsQueryPort;
import be.xplore.githubmetrics.githubadapter.config.GithubConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class GithubAdapter implements WorkflowRunsQueryPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(GithubAdapter.class);
    private final GithubConfig config;
    private final ObjectMapper objectMapper;

    public GithubAdapter(GithubConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<WorkflowRun> getLastDaysWorkflows() {
        try {
            LOGGER.debug("running request");
            var response = this.getResponse();
            var list = this.parseResponse(response);
            LOGGER.debug("number of unique workflow runs: {}", list.size());
            return list;

        } catch (IOException | InterruptedException | URISyntaxException e) {
            LOGGER.debug("", e);
            throw new RuntimeException(e);
        }
    }

    private List<WorkflowRun> parseResponse(HttpResponse<String> response) throws JsonProcessingException {
        JsonNode jsonBody = this.objectMapper.readTree(response.body());

        var arr = jsonBody.get("workflow_runs");
        var list = new ArrayList<WorkflowRun>();
        if (arr != null) {
            for (final JsonNode workflow_run : arr) {
                list.add(new WorkflowRun(workflow_run));
            }
        }
        return list;
    }

    private HttpResponse<String> getResponse() throws IOException, InterruptedException, URISyntaxException {
        var client = HttpClient.newHttpClient();
        var parameterMap = new HashMap<String, Object>();
        parameterMap.put("created", ">=2024-04-10");
        var request = HttpRequest.newBuilder()
                .uri(UriComponentsBuilder.newInstance()
                        .uri(new URI("https://api.github.com/repos/github-insights/github-metrics/actions/runs"))
                        .uriVariables(parameterMap)
                        .build().toUri()
                )
                .header("X-Github-Api-Version", "2022-11-28")
                .headers("Authorization", this.config.token())
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
