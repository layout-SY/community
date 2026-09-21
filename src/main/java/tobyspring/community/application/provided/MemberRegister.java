package tobyspring.community.application.provided;

import tobyspring.community.domain.Member;
import tobyspring.community.domain.RegisterMemberRequest;

public interface MemberRegister {
    Member register(RegisterMemberRequest registerMemberRequest);
}
