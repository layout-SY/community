package tobyspring.community.adapter.webapi.member;

import tobyspring.community.domain.member.Member;
import tobyspring.community.domain.member.MemberDetail;
import tobyspring.community.domain.member.MemberStatus;

public record MemberDto(
        Long id,
        String email,
        String nickname,
        MemberStatus status,
        MemberDetail detail
) {
    public static MemberDto mapper(Member member) {
        return new MemberDto(
                member.getId(),
                member.getEmail().getAddress(),
                member.getNickname(),
                member.getStatus(),
                member.getDetail()
        );
    }
}
