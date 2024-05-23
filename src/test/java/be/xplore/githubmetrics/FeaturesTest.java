package be.xplore.githubmetrics;

import be.xplore.githubmetrics.domain.repository.GetAllRepositoriesUseCase;
import be.xplore.githubmetrics.prometheusexporter.features.Features;
import be.xplore.githubmetrics.prometheusexporter.repository.RepositoryCountExporter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.togglz.junit5.AllDisabled;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
class FeaturesTest {

    @MockBean
    private GetAllRepositoriesUseCase mockUseCase;
    @Autowired
    private RepositoryCountExporter repositoryCountExporter;

    @Test
    @AllDisabled(Features.class)
    void RepositoryFeatureTurnedOffRepositoryExportShouldntRun() {
        repositoryCountExporter.run();

        verify(mockUseCase, never()).getAllRepositories();
    }
}
