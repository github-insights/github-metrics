package be.xplore.githubmetrics.prometheusexporter.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;

public enum Features implements Feature {


    EXPORTER_JOBS_FEATURE,
    EXPORTER_WORKFLOW_RUNS_FEATURE,
    EXPORTER_ACTIVE_WORKFLOW_RUNS_FEATURE,
    EXPORTER_WORKFLOW_RUN_BUILD_TIMES_FEATURE,
    EXPORTER_SELF_HOSTED_RUNNERS_FEATURE,
    EXPORTER_REPOSITORIES_FEATURE,
    EXPORTER_PULL_REQUESTS_FEATURE;

    private final Logger logger = LoggerFactory.getLogger(Features.class);

    Features() {
        logger.info("Feature {} is active.", this);
    }

    @Override
    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }
}
