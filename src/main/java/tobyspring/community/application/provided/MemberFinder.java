package tobyspring.community.application.provided;

import tobyspring.community.domain.Member;

public interface MemberFinder {
    Member findById(Long memberId);
}
