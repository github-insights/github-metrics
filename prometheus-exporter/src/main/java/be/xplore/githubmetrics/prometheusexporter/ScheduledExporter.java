package be.xplore.githubmetrics.prometheusexporter;

public interface ScheduledExporter {

    void run();

    String cronExpression();
}
