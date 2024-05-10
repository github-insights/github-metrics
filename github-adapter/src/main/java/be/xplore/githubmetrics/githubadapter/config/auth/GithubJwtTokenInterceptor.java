package be.xplore.githubmetrics.githubadapter.config.auth;

import be.xplore.githubmetrics.githubadapter.config.GithubProperties;
import be.xplore.githubmetrics.githubadapter.exceptions.UnableToAuthenticateGithubAppException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Date;

@Component
public class GithubJwtTokenInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(GithubJwtTokenInterceptor.class);
    private final GithubProperties githubProperties;

    public GithubJwtTokenInterceptor(GithubProperties githubProperties) {
        this.githubProperties = githubProperties;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders().setBearerAuth(this.getAppIdJwt());
        return execution.execute(request, body);
    }

    private String getAppIdJwt() {
        LOGGER.debug("Generating JWT from GH APP ID");
        try {
            return generateJwt();
        } catch (JOSEException e) {
            throw new UnableToAuthenticateGithubAppException("Was unable to parse Github App Token", e);
        }
    }

    private String generateJwt() throws JOSEException {
        JWK jwk = JWK.parseFromPEMEncodedObjects(githubProperties.application().pem());

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256)
                        .keyID(jwk.getKeyID())
                        .build(),
                new JWTClaimsSet.Builder()
                        .issuer(this.githubProperties.application().id())
                        .issueTime(this.getDateOneMinuteAgo())
                        .expirationTime(this.getDateInNineMinutes())
                        .build()
        );

        signedJWT.sign(new RSASSASigner(jwk.toRSAKey()));

        return signedJWT.serialize();
    }

    private Date getDateOneMinuteAgo() {
        return toDate(now().minusMinutes(1));
    }

    private Date getDateInNineMinutes() {
        return toDate(now().plusMinutes(9));
    }

    private Date toDate(ZonedDateTime dateTime) {
        return Date.from(dateTime.toInstant());
    }

    private ZonedDateTime now() {
        return ZonedDateTime.now();
    }
}
