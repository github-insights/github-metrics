package be.xplore.githubmetrics.prometheusexporter.features;

import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;

public enum Features implements Feature {

    EXPORTER_JOBS_FEATURE,
    EXPORTER_WORKFLOW_RUNS_FEATURE,
    EXPORTER_WORKFLOW_RUN_BUILD_TIMES_FEATURE,
    EXPORTER_SELF_HOSTED_RUNNERS_EXPORTER,
    EXPORTER_REPOSITORIES_FEATURE,
    EXPORTER_PULL_REQUESTS_FEATURE;

    @Override
    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }
}
