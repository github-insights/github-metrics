package be.xplore.githubmetrics.prometheusexporter.repository;

import be.xplore.githubmetrics.domain.repository.GetAllRepositoriesUseCase;
import be.xplore.githubmetrics.prometheusexporter.ScheduledExporter;
import be.xplore.githubmetrics.prometheusexporter.StartupExporter;
import be.xplore.githubmetrics.prometheusexporter.config.SchedulingProperties;
import be.xplore.githubmetrics.prometheusexporter.features.FeatureAssociation;
import be.xplore.githubmetrics.prometheusexporter.features.Features;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RepositoryCountExporter implements ScheduledExporter, StartupExporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryCountExporter.class);
    private final GetAllRepositoriesUseCase getAllRepositoriesUseCase;
    private final MeterRegistry registry;
    private final String cronExpression;

    public RepositoryCountExporter(
            GetAllRepositoriesUseCase getAllRepositoriesUseCase,
            SchedulingProperties schedulingProperties,
            MeterRegistry meterRegistry
    ) {
        this.getAllRepositoriesUseCase = getAllRepositoriesUseCase;
        this.registry = meterRegistry;
        this.cronExpression = schedulingProperties.repositoryCountInterval();
    }

    private void retrieveAndExportRepositoryCount() {
        LOGGER.info("RepositoryCount scheduled task is running.");

        var repositories = this.getAllRepositoriesUseCase.getAllRepositories();

        Gauge.builder(
                        "repositories_count",
                        repositories,
                        List::size
                )
                .strongReference(true)
                .register(this.registry);

        LOGGER.debug("RepositoryCount scheduled task finished.");
    }

    @Override
    @FeatureAssociation(value = Features.EXPORTER_REPOSITORIES_FEATURE)
    public void run() {
        this.retrieveAndExportRepositoryCount();
    }

    @Override
    public String cronExpression() {
        return this.cronExpression;
    }
}
