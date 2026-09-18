package tobyspring.community.domain;

import tobyspring.community.application.provided.Email;

public record RegisterMemberRequest(
        String email,
        String nickname,
        String password
) {
}
