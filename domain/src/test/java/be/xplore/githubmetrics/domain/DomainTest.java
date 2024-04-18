package be.xplore.githubmetrics.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DomainTest {
    @Test
    public void testDomain() {
        assertEquals(
                DomainHello.hello(),
                "Domain"
        );
    }

}
