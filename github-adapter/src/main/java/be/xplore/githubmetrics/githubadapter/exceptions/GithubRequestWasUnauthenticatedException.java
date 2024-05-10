package be.xplore.githubmetrics.githubadapter.exceptions;

import be.xplore.githubmetrics.domain.exceptions.GenericAdapterException;

public class GithubRequestWasUnauthenticatedException extends GenericAdapterException {
    public GithubRequestWasUnauthenticatedException(String message, Throwable cause) {
        super(message, cause);
    }

    public GithubRequestWasUnauthenticatedException(String message) {
        super(message);
    }
}
