package be.xplore.githubmetrics.githubadapter.exceptions;

import be.xplore.githubmetrics.domain.exceptions.GenericAdapterException;

public class InvalidAdapterRequestURIException extends GenericAdapterException {
    public InvalidAdapterRequestURIException(String message) {
        super(message);
    }
}
