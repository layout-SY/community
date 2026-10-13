package tobyspring.community.application.provided;

import tobyspring.community.domain.member.LoginResponseDto;
import tobyspring.community.domain.member.RegisterMemberRequest;
import tobyspring.community.domain.shared.Email;

public interface MemberLogin {
    LoginResponseDto login(Email email, String password);
}
