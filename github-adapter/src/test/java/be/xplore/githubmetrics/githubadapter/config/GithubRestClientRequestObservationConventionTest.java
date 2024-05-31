package be.xplore.githubmetrics.githubadapter.config;

import io.micrometer.common.KeyValues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.observation.ClientRequestObservationContext;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GithubRestClientRequestObservationConventionTest {
    private static final String PATH = "path";
    private static final String URI = "uri";
    private static final String STATUS = "status";

    private GithubRestClientRequestObservationConvention observationConvention;
    private ClientRequestObservationContext mockObservationContext;
    private HttpHeaders mockHeaders;

    @BeforeEach
    void setUp() {
        this.observationConvention = new GithubRestClientRequestObservationConvention();
        this.mockObservationContext = mock(ClientRequestObservationContext.class);
        ClientHttpRequest mockCarrier = mock(ClientHttpRequest.class);
        this.mockHeaders = mock(HttpHeaders.class);
        when(mockObservationContext.getCarrier()).thenReturn(mockCarrier);
        when(mockCarrier.getHeaders()).thenReturn(mockHeaders);
        when(mockCarrier.getMethod()).thenReturn(HttpMethod.GET);
    }

    @Test
    void conventionShouldReturnNOHEADERifNoValue() {
        when(mockHeaders.get(PATH)).thenReturn(List.of());
        assertTrue(keyValPresent(URI, "NONE"));
    }

    @Test
    void conventionShouldReturnNOHEADERifNoHeader() {
        assertTrue(keyValPresent(URI, "NONE"));
    }

    @Test
    void emptyContextResponseShouldTranslateToEmptyResponseCodeLabel() {
        assertTrue(keyValPresent(STATUS, ""));
    }

    @Test
    void conventionResponseCodesShouldTranslateToCorrectStrings() throws IOException {
        try (ClientHttpResponse response = mock(ClientHttpResponse.class)) {
            when(mockObservationContext.getResponse()).thenReturn(response);
            setupResponse(response);
            checkStatusCodes();
        }
    }

    @Test
    void responseCodeThrowingExceptionShouldStillReturnStatus() throws IOException {
        try (ClientHttpResponse response = mock(ClientHttpResponse.class)) {
            when(mockObservationContext.getResponse()).thenReturn(response);
            when(response.getStatusCode()).thenThrow(IOException.class);

            assertTrue(keyValPresent(STATUS, ""));
        }
    }

    private void setupResponse(ClientHttpResponse response) throws IOException {
        when(response.getStatusCode())
                .thenReturn(HttpStatusCode.valueOf(101))
                .thenReturn(HttpStatusCode.valueOf(201))
                .thenReturn(HttpStatusCode.valueOf(301))
                .thenReturn(HttpStatusCode.valueOf(401))
                .thenReturn(HttpStatusCode.valueOf(501));
    }

    private void checkStatusCodes() {
        assertTrue(keyValPresent(STATUS, "1xx"));
        assertTrue(keyValPresent(STATUS, "2xx"));
        assertTrue(keyValPresent(STATUS, "3xx"));
        assertTrue(keyValPresent(STATUS, "4xx"));
        assertTrue(keyValPresent(STATUS, "5xx"));
    }

    private boolean keyValPresent(String key, String val) {
        return get().stream().anyMatch(keyValue ->
                keyValue.getKey().equals(key) && keyValue.getValue().equals(val)
        );
    }

    private KeyValues get() {
        return this.observationConvention.getLowCardinalityKeyValues(this.mockObservationContext);
    }

}
