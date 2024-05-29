package be.xplore.githubmetrics.githubadapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Component
public class GithubApiUtilities {

    private static final Logger LOGGER = LoggerFactory.getLogger(GithubApiUtilities.class);
    private final RestClient restClient;

    public GithubApiUtilities(
            @Qualifier("defaultRestClient") RestClient restClient
    ) {
        this.restClient = restClient;
    }

    public Function<UriBuilder, URI> setPathAndParameters(
            String path,
            List<Object> uriVariables
    ) {
        return uriBuilder ->
                uriBuilder.path(path)
                        .build(uriVariables.toArray(new Object[0]));
    }

    public Function<UriBuilder, URI> setPathAndParameters(
            String path,
            List<Object> uriVariables,
            Map<String, String> parameters
    ) {
        return uriBuilder -> {
            uriBuilder.path(path);
            parameters.forEach(uriBuilder::queryParam);
            return uriBuilder.build(uriVariables.toArray(new Object[0]));
        };
    }

    public Function<UriBuilder, URI> setPathAndParameters(
            String path,
            Map<String, String> parameters
    ) {
        return uriBuilder -> {
            uriBuilder.path(path);
            parameters.forEach(uriBuilder::queryParam);
            return uriBuilder.build();
        };
    }

    public <D, A> List<D> followPaginationLink(
            ResponseEntity<A> responseEntity,
            Function<A, List<D>> conversionFunction,
            Class<A> clazz
    ) {
        List<D> objects = new ArrayList<>(conversionFunction.apply(
                responseEntity.getBody()
        ));

        return getNextPageLink(responseEntity.getHeaders()).map(link -> {
            LOGGER.trace("Parsed link was: {}", link);

            List<D> nextObjects = this.followPaginationLink(
                    getNextResponseEntity(link, clazz),
                    conversionFunction,
                    clazz
            );

            LOGGER.trace(
                    "Adding {} objects to already present {} objects.",
                    nextObjects.size(),
                    objects.size()
            );
            objects.addAll(nextObjects);
            return objects;
        }).orElse(objects);

    }

    private <A> ResponseEntity<A> getNextResponseEntity(
            String link,
            Class<A> clazz
    ) {
        return this.restClient.get()
                .uri(link)
                .retrieve()
                .toEntity(clazz);
    }

    private Optional<String> getNextPageLink(HttpHeaders headers) {
        return Optional.ofNullable(headers.get("link")).flatMap(headerList -> {
            LOGGER.trace("Valid link header is present.");
            var headersConcatenated = String.join(" ", headerList);
            var partsWithNext = Arrays.stream(headersConcatenated.split(","))
                    .filter(part -> part.contains("rel=\"next\""))
                    .toList();

            Optional<String> nextPageLink = Optional.empty();
            if (!partsWithNext.isEmpty()) {
                nextPageLink = parseLinkOutOfHeader(partsWithNext.getFirst());
            }
            return nextPageLink;
        });
    }

    private Optional<String> parseLinkOutOfHeader(String partWithNext) {
        return Arrays.stream(partWithNext.split(";"))
                .findFirst()
                .map(bracedLink -> {
                    LOGGER.trace("Parsed link before final cleanup is: {}", bracedLink);
                    var trimmedLink = bracedLink.trim();
                    return trimmedLink.substring(1, trimmedLink.length() - 1);
                });
    }
}