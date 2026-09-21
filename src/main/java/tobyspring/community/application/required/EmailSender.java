package tobyspring.community.application.required;

import tobyspring.community.domain.Email;

public interface EmailSender {
    boolean send(Email email, String subject, String body);
}
