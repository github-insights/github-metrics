package be.xplore.githubmetrics.prometheusexporter.selfhostedrunner;

import be.xplore.githubmetrics.domain.selfhostedrunner.GetAllSelfHostedRunnersUseCase;
import be.xplore.githubmetrics.domain.selfhostedrunner.OperatingSystem;
import be.xplore.githubmetrics.domain.selfhostedrunner.SelfHostedRunner;
import be.xplore.githubmetrics.domain.selfhostedrunner.SelfHostedRunnerStatus;
import be.xplore.githubmetrics.prometheusexporter.config.SchedulingProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SelfHostedRunnerCountsExporterTest {
    private final GetAllSelfHostedRunnersUseCase getAllSelfHostedRunnersUseCase = mock(GetAllSelfHostedRunnersUseCase.class);
    private final SchedulingProperties schedulingProperties = mock(SchedulingProperties.class);
    private MeterRegistry registry;
    private SelfHostedRunnerCountsExporter selfHostedRunnerCountsExporter;

    @BeforeAll
    static void beforeAll() {

    }

    private List<SelfHostedRunner> getSelfHostedRunners() {
        return List.of(
                new SelfHostedRunner(OperatingSystem.MAC_OS, SelfHostedRunnerStatus.BUSY),
                new SelfHostedRunner(OperatingSystem.LINUX, SelfHostedRunnerStatus.BUSY),
                new SelfHostedRunner(OperatingSystem.WINDOWS, SelfHostedRunnerStatus.BUSY),
                new SelfHostedRunner(OperatingSystem.MAC_OS, SelfHostedRunnerStatus.IDLE),
                new SelfHostedRunner(OperatingSystem.LINUX, SelfHostedRunnerStatus.IDLE),
                new SelfHostedRunner(OperatingSystem.WINDOWS, SelfHostedRunnerStatus.IDLE),
                new SelfHostedRunner(OperatingSystem.MAC_OS, SelfHostedRunnerStatus.OFFLINE),
                new SelfHostedRunner(OperatingSystem.LINUX, SelfHostedRunnerStatus.OFFLINE),
                new SelfHostedRunner(OperatingSystem.WINDOWS, SelfHostedRunnerStatus.OFFLINE),
                new SelfHostedRunner(OperatingSystem.MAC_OS, SelfHostedRunnerStatus.BUSY),
                new SelfHostedRunner(OperatingSystem.WINDOWS, SelfHostedRunnerStatus.IDLE)
        );
    }

    @BeforeEach
    void setUp() {
        when(this.getAllSelfHostedRunnersUseCase.getAllSelfHostedRunners()).thenReturn(
                this.getSelfHostedRunners()
        );

        this.registry = new SimpleMeterRegistry();
        this.selfHostedRunnerCountsExporter = new SelfHostedRunnerCountsExporter(
                this.getAllSelfHostedRunnersUseCase,
                schedulingProperties,
                this.registry
        );
    }

    @Test
    void listOfSelfHostedRunnersShouldBeCountedInTheCorrectWay() {
        selfHostedRunnerCountsExporter.run();
        Arrays.stream(OperatingSystem.values()).forEach(os ->
                Arrays.stream(SelfHostedRunnerStatus.values()).forEach(
                        status ->
                                assertEquals(
                                        getSelfHostedRunners().stream().filter(runner ->
                                                runner.getOperatingSystem().equals(os) &&
                                                        runner.getSelfHostedRunnerStatus().equals(status)
                                        ).count(),
                                        this.registry.find("self_hosted_runners")
                                                .tags("os", os.toString(), "status", status.toString())
                                                .gauge().value()
                                )
                )
        );
    }

}
