package be.xplore.githubmetrics.githubadapter.exceptions;

import be.xplore.githubmetrics.domain.exceptions.GenericAdapterException;

public class UnableToParseGHRepositoryArrayException extends GenericAdapterException {
    public UnableToParseGHRepositoryArrayException(String message) {
        super(message);
    }
}
