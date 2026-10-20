package tobyspring.community.application.member.required;

import tobyspring.community.domain.shared.Email;

public interface EmailSender {
    void send(Email email, String subject, String body);
}
