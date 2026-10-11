package tobyspring.community.application.provided;

import tobyspring.community.domain.member.Member;
import tobyspring.community.domain.shared.Email;

public interface MemberFinder {
    Member findById(Long memberId);

    Member findByEmail(Email email);
}
