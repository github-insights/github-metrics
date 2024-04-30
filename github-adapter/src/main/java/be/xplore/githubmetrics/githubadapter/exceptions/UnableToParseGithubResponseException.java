package be.xplore.githubmetrics.githubadapter.exceptions;

import be.xplore.githubmetrics.domain.exceptions.GenericAdapterException;

public class UnableToParseGithubResponseException extends GenericAdapterException {
    public UnableToParseGithubResponseException(String message) {
        super(message);
    }

    public UnableToParseGithubResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
