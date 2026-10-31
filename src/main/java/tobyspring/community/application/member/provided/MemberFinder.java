package tobyspring.community.application.member.provided;

import tobyspring.community.domain.member.Member;
import tobyspring.community.domain.shared.Email;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface MemberFinder {
    Member findById(Long memberId);

    Member findByEmail(Email email);

    Map<Long, String> getNicknameByIds(Collection<Long> memberIds);
}
