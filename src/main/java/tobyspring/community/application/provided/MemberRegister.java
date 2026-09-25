package tobyspring.community.application.provided;

import jakarta.validation.Valid;
import tobyspring.community.domain.Member;
import tobyspring.community.domain.RegisterMemberRequest;

public interface MemberRegister {
    Member register(@Valid RegisterMemberRequest registerMemberRequest);

    Member activate(Long memberId);

    Member deactivate(Long memberId);
}
