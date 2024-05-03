package be.xplore.githubmetrics.githubadapter.config;

import be.xplore.githubmetrics.githubadapter.exceptions.UnableToAuthenticateGithubAppException;
import be.xplore.githubmetrics.githubadapter.mappingclasses.GHAppInstallationAccessToken;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Consumer;

@Component
public class GithubApiAuthorization {
    private static final Logger LOGGER = LoggerFactory.getLogger(GithubApiAuthorization.class);
    private final GithubConfig githubConfig;
    private final RestClient restClient;

    public GithubApiAuthorization(GithubConfig githubConfig, RestClient restClient) {
        this.githubConfig = githubConfig;
        this.restClient = restClient;
    }

    public Consumer<HttpHeaders> getAuthHeader() {
        LOGGER.debug("Getting Auth Header From Installation id + app id + app private key");
        var jwtToken = this.createAppIdJwt();
        var accessToken = this.restClient.post()
                .uri(MessageFormat.format(
                        "{0}://{1}:{2}/app/installations/{3}/access_tokens",
                        githubConfig.schema(),
                        githubConfig.host(),
                        githubConfig.port(),
                        githubConfig.application().installId()))
                .headers(httpHeaders -> httpHeaders.setBearerAuth(
                        jwtToken
                ))
                .retrieve().body(GHAppInstallationAccessToken.class);

        if (accessToken == null) {
            throw new UnableToAuthenticateGithubAppException("Unable to Get Access token from Github");
        }

        return header -> header.setBearerAuth(accessToken.token());
    }

    private String createAppIdJwt() {
        LOGGER.debug("Generating JWT from GH APP ID");
        try {
            JWK jwk = JWK.parseFromPEMEncodedObjects(githubConfig.application().pem());

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256)
                            .keyID(jwk.getKeyID())
                            .build(),
                    new JWTClaimsSet.Builder()
                            .issuer(this.githubConfig.application().id())
                            .issueTime(this.getDateOneMinuteAgo())
                            .expirationTime(this.getDateInTenMinutes())
                            .build()
            );

            signedJWT.sign(new RSASSASigner(jwk.toRSAKey()));

            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new UnableToAuthenticateGithubAppException("Was unable to parse Github App Token", e);
        }
    }

    private Date getDateOneMinuteAgo() {
        var calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, -1);
        return calendar.getTime();
    }

    private Date getDateInTenMinutes() {
        var calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, 10);
        return calendar.getTime();
    }
}
