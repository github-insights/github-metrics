package be.xplore.githubmetrics.githubadapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Component
public class GithubApiUtilities {

    private static final Logger LOGGER = LoggerFactory.getLogger(GithubApiUtilities.class);

    @SuppressWarnings("checkstyle:ParameterNumber")
    public <D, A> List<D> followPaginationLink(
            ResponseEntity<A> responseEntity,
            int page,
            Function<Integer, ResponseEntity<A>> fetchFunction,
            Function<A, List<D>> conversionFunction
    ) {
        List<D> objects = new ArrayList<>(conversionFunction.apply(
                responseEntity.getBody()
        ));

        return getNextPageLink(responseEntity.getHeaders()).map(link -> {
            LOGGER.trace("Parsed link was: {}", link);

            List<D> nextObjects = this.followPaginationLink(
                    fetchFunction.apply(page),
                    page + 1,
                    fetchFunction,
                    conversionFunction
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