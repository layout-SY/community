package tobyspring.community.application.required;

import tobyspring.community.domain.shared.Email;

public interface EmailSender {
    void send(Email email, String subject, String body);
}
