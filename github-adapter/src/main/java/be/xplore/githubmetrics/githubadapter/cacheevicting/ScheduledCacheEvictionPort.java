package be.xplore.githubmetrics.githubadapter.cacheevicting;

public interface ScheduledCacheEvictionPort {

    boolean freshDataCanWait();

    String cacheName();

    String cronExpression();

}
