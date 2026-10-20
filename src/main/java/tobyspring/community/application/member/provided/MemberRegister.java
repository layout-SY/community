package tobyspring.community.application.member.provided;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import tobyspring.community.domain.member.Member;
import tobyspring.community.domain.member.MemberDetailRequest;
import tobyspring.community.domain.member.RegisterMemberRequest;

public interface MemberRegister {
    Member register(@Valid RegisterMemberRequest registerMemberRequest);

    Member activate(Long memberId);

    Member deactivate(Long memberId);

    Member updateInfo(Long memberId, @Size(min = 5, max = 20) String nickname, @Valid MemberDetailRequest detailRequest);
}
