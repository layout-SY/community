package tobyspring.community.application.member.exception;

public class AlreadyStateException extends RuntimeException {
    public AlreadyStateException(String message) {
        super(message);
    }
}
