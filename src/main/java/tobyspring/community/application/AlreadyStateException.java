package tobyspring.community.application;

public class AlreadyStateException extends RuntimeException {
    public AlreadyStateException(String message) {
        super(message);
    }
}
