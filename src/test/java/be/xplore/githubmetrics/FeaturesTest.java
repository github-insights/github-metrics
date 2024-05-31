package be.xplore.githubmetrics;

import be.xplore.githubmetrics.domain.repository.GetAllRepositoriesUseCase;
import be.xplore.githubmetrics.prometheusexporter.repository.RepositoryCountExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class FeaturesTest {

    @MockBean
    private GetAllRepositoriesUseCase mockUseCase;
    @Autowired
    private RepositoryCountExporter repositoryCountExporter;

    /*@Test
    @AllDisabled(Features.class)
    void RepositoryFeatureTurnedOffRepositoryExportShouldntRun() {
        repositoryCountExporter.run();

        verify(mockUseCase, never()).getAllRepositories();
    }*/
}
