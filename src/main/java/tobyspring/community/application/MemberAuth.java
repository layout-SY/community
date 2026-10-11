package tobyspring.community.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import tobyspring.community.adapter.security.AuthenticateException;
import tobyspring.community.adapter.security.PasswordEncode;
import tobyspring.community.adapter.security.TokenManager;
import tobyspring.community.application.provided.MemberFinder;
import tobyspring.community.application.provided.MemberLogin;
import tobyspring.community.domain.member.LoginResponseDto;
import tobyspring.community.domain.member.Member;
import tobyspring.community.domain.member.RegisterMemberRequest;
import tobyspring.community.domain.member.TokenResult;
import tobyspring.community.domain.shared.Email;

@Service
@RequiredArgsConstructor
public class MemberAuth implements MemberLogin {
    private final MemberFinder memberFinder;
    private final PasswordEncode passwordEncode;
    private final TokenManager tokenManager;

    @Override
    public LoginResponseDto login(Email email, String password) {
        Member byEmail = memberFinder.findByEmail(email);

        TokenResult tokenResult = null;
        if (passwordEncode.match(password, byEmail.getPasswordHash())){
            tokenResult = tokenManager.generateToken(byEmail);
        } else {
            throw new AuthenticateException("비밀번호가 틀렸습니다");
        }

        return new LoginResponseDto(byEmail, tokenResult);
    }

    @Override
    public String logout(RegisterMemberRequest request) {
        return "";
    }
}
