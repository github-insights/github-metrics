package be.xplore.githubmetrics.githubadapter.exceptions;

import be.xplore.githubmetrics.domain.exceptions.GenericAdapterException;

public class UnableToParseGHActionRunsException extends GenericAdapterException {
    public UnableToParseGHActionRunsException(String message) {
        super(message);
    }
}
