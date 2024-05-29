package be.xplore.githubmetrics.githubadapter.mappingclasses;

import java.time.Instant;
import java.time.ZonedDateTime;

public record GHAppInstallationAccessToken(
        String token,
        String expires_at
) {
    public static final String PATH = "/app/installations/{appId}/access_tokens";

    public ZonedDateTime getActualDate() {
        return Instant
                .parse(this.expires_at)
                .atZone(ZonedDateTime.now().getZone());
    }
}
