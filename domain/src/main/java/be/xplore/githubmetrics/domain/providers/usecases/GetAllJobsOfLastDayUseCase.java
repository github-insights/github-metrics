package be.xplore.githubmetrics.domain.providers.usecases;

import be.xplore.githubmetrics.domain.domain.Job;

import java.util.List;

public interface GetAllJobsOfLastDayUseCase {
    List<Job> getAllJobsOfLastDay();
}
