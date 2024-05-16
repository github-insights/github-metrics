package be.xplore.githubmetrics.domain.pullrequest;

import java.time.ZonedDateTime;

public class PullRequest {
    private final long id;
    private final ZonedDateTime createdAt;
    private ZonedDateTime closedAt;
    private ZonedDateTime mergedAt;

    public PullRequest(long id, ZonedDateTime createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    public PullRequest(
            long id,
            ZonedDateTime createdAt,
            ZonedDateTime closedAt,
            ZonedDateTime mergedAt
    ) {
        this.id = id;
        this.createdAt = createdAt;
        this.closedAt = closedAt;
        this.mergedAt = mergedAt;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public long getId() {
        return id;
    }

    public PullRequestState getState() {
        PullRequestState pullRequestState = PullRequestState.OPEN;
        if (this.mergedAt != null) {
            pullRequestState = PullRequestState.MERGED;
        } else if (this.closedAt != null) {
            pullRequestState = PullRequestState.CLOSED;
        }
        return pullRequestState;
    }

    public void setClosedAt(ZonedDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public void setMergedAt(ZonedDateTime mergedAt) {
        this.mergedAt = mergedAt;
    }
}
