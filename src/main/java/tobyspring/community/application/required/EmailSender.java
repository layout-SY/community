package tobyspring.community.application.required;

public interface EmailSender {
    boolean send(String email, String subject, String body);
}
