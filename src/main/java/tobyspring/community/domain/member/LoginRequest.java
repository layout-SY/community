package tobyspring.community.domain.member;

import tobyspring.community.domain.shared.Email;

public record LoginRequest(
        Email email,
        String password
) {
}
