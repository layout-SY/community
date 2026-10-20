package tobyspring.community.application.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tobyspring.community.adapter.security.PasswordEncode;
import tobyspring.community.adapter.security.TokenManager;
import tobyspring.community.application.member.exception.DeactivateMemberException;
import tobyspring.community.application.member.exception.NotFoundException;
import tobyspring.community.application.member.provided.MemberFinder;
import tobyspring.community.application.member.provided.MemberLogin;
import tobyspring.community.domain.member.*;
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

        if(byEmail.getStatus().equals(MemberStatus.DEACTIVATED)) {
            throw new DeactivateMemberException("비활성화된 계정입니다.");
        }

        TokenResult tokenResult = null;
        if (passwordEncode.match(password, byEmail.getPasswordHash())){
            tokenResult = tokenManager.generateToken(byEmail);
        } else {
            throw new NotFoundException("입력한 회원정보와 일치하는 회원을 찾을 수 없습니다.");
        }

        return new LoginResponseDto(byEmail, tokenResult);
    }
}
