package be.xplore.githubmetrics.githubadapter.mappingclasses;

import be.xplore.githubmetrics.domain.pullrequest.PullRequest;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public record GHPullRequest(
        long id,
        String created_at,
        String closed_at,
        String merged_at
) {
    private ZonedDateTime getZonedDateTime(String date) {
        return Instant.parse(date).atZone(ZoneId.of("Etc/UTC"));
    }

    public PullRequest getPullRequest() {
        PullRequest pullRequest = new PullRequest(
                this.id,
                getZonedDateTime(this.created_at)
        );
        if (this.closed_at != null) {
            pullRequest.setClosedAt(getZonedDateTime(this.closed_at()));
        }
        if (this.merged_at != null) {
            pullRequest.setMergedAt(getZonedDateTime(this.merged_at()));
        }

        return pullRequest;
    }
}
