package be.xplore.githubmetrics.githubadapter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@ConfigurationProperties(prefix = "app.github")
public record GithubProperties(
        String url,
        String org,
        Application application,
        RateLimiting ratelimiting,
        Parsing parsing
) {
    public record Application(
            String id,
            String installId,
            String pem
    ) {
    }

    public record RateLimiting(
            long secondsBetweenStateRecalculations,
            double rateLimitBuffer,
            double criticalLimit,
            double warningLimit,
            double concerningLimit,
            double goodLimit
    ) {
    }

    public record Parsing(
            SelfHostedRunnerOsKeywords selfHostedRunnerOsKeywords
    ) {
        public record SelfHostedRunnerOsKeywords(
                String linux,
                String windows,
                String macos
        ) {

            public List<String> linuxKeywords() {
                return splitCleanAndAdd(this.linux, "linux");
            }

            public List<String> windowsKeywords() {
                return splitCleanAndAdd(this.windows, "windows");
            }

            public List<String> macosKeywords() {
                return splitCleanAndAdd(this.macos, "macos");
            }

            private List<String> splitCleanAndAdd(String keywords, String... defaultKeywords) {
                List<String> cleanedKeywords = Stream.concat(
                                Arrays.stream(keywords.split(",")),
                                Arrays.stream(defaultKeywords)
                        )
                        .map(keyword -> keyword.toLowerCase(Locale.ROOT))
                        .filter(word -> !word.isBlank()).toList();
                return new ArrayList<>(cleanedKeywords);
            }

        }

    }
}