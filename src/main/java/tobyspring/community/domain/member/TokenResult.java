package tobyspring.community.domain.member;

public record TokenResult(
        String accessToken,
        String refreshToken
) {}
