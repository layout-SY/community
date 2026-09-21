package tobyspring.community.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import tobyspring.community.application.provided.MemberRegister;
import tobyspring.community.application.required.EmailSender;
import tobyspring.community.application.required.MemberRepository;
import tobyspring.community.domain.Email;
import tobyspring.community.domain.PasswordEncode;
import tobyspring.community.domain.Member;
import tobyspring.community.domain.RegisterMemberRequest;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService implements MemberRegister {
    private final MemberRepository memberRepository;
    private final PasswordEncode passwordEncode;
    private final EmailSender emailSender;

    @Override
    public Member register(RegisterMemberRequest registerMemberRequest) {
        Optional<Member> byEmail = memberRepository.findByEmail(new Email(registerMemberRequest.email()));
        if(byEmail.isPresent()) {
            throw new DuplicateEmailException(registerMemberRequest.email());
        };

        Member member = Member.register(registerMemberRequest, passwordEncode);

        memberRepository.save(member);

        emailSender.send(member.getEmail(), "ㅇㅣㅁㅔㅇㅣㄹ ㅂㅏㄹㅅㅗㅇ", "ㅇㅣㅁㅔㅇㅣㄹ");

        return member;
    }
}
