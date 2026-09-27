package tobyspring.community.application.provided;

import tobyspring.community.domain.member.Member;

public interface MemberFinder {
    Member findById(Long memberId);
}
