package be.xplore.githubmetrics.githubadapter.exceptions;

import be.xplore.githubmetrics.domain.exceptions.GenericAdapterException;

public class UnableToAuthenticateGithubAppException extends GenericAdapterException {
    public UnableToAuthenticateGithubAppException(String message) {
        super(message);
    }

    public UnableToAuthenticateGithubAppException(String message, Throwable cause) {
        super(message, cause);
    }
}
