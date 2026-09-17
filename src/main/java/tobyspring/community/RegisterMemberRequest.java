package tobyspring.community;

public record RegisterMemberRequest(
        Email email,
        String nickname,
        String password
) {
}
