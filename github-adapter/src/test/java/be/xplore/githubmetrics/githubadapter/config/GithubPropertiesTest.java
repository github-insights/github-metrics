package be.xplore.githubmetrics.githubadapter.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GithubPropertiesTest {

    private GithubProperties.Parsing.SelfHostedRunnerOsKeywords osKeywords;

    @BeforeEach
    void setUp() {
        this.osKeywords = new GithubProperties.Parsing.SelfHostedRunnerOsKeywords(
                "somestring,someotherString",
                " ",
                "value"
        );
    }

    @Test
    void commaSeparatedListShouldBeSplitCorrectly() {
        List<String> keywords = osKeywords.linuxKeywords();

        assertEquals(List.of("somestring", "someotherstring", "linux"), keywords);
    }

    @Test
    void blankStringShouldStillOnlyTranslateToDefaultValue() {
        List<String> keywords = osKeywords.windowsKeywords();
        assertEquals(List.of("windows"), keywords);
    }

    @Test
    void singleValueShouldBeAddedCorrectly() {
        List<String> keywords = osKeywords.macosKeywords();
        assertEquals(List.of("value", "macos"), keywords);
    }
}
