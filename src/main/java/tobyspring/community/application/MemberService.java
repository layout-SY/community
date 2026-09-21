package tobyspring.community.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tobyspring.community.application.provided.MemberRegister;
import tobyspring.community.application.required.EmailSender;
import tobyspring.community.application.required.MemberRepository;
import tobyspring.community.domain.PasswordEncode;
import tobyspring.community.domain.Member;
import tobyspring.community.domain.RegisterMemberRequest;

@Service
@RequiredArgsConstructor
public class MemberService implements MemberRegister {
    private final MemberRepository memberRepository;
    private final PasswordEncode passwordEncode;
    private final EmailSender emailSender;

    @Override
    public Member register(RegisterMemberRequest registerMemberRequest) {
        Member member = Member.register(registerMemberRequest, passwordEncode);

        memberRepository.save(member);

        emailSender.send(member.getEmail(), "ㅇㅣㅁㅔㅇㅣㄹ ㅂㅏㄹㅅㅗㅇ", "ㅇㅣㅁㅔㅇㅣㄹ");

        return member;
    }
}
