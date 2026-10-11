package tobyspring.community.domain.member;

public record LoginResponseDto(
        Member member,
        TokenResult token
) {
}
