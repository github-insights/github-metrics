package be.xplore.githubmetrics.domain.exceptions;

public class GenericAdapterException extends RuntimeException {
    public GenericAdapterException(String message, Throwable cause) {
        super(message, cause);
    }

    public GenericAdapterException(String message) {
        super(message);
    }

}
