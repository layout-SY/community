package tobyspring.community.domain;

public record RegisterMemberRequest(
        String email,
        String nickname,
        String password
) {
}
