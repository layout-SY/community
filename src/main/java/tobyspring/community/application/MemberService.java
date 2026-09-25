package tobyspring.community.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import tobyspring.community.application.provided.MemberRegister;
import tobyspring.community.application.required.EmailSender;
import tobyspring.community.application.required.MemberRepository;
import tobyspring.community.domain.*;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Validated
public class MemberService implements MemberRegister {
    private final MemberRepository memberRepository;
    private final PasswordEncode passwordEncode;
    private final EmailSender emailSender;

    @Override
    public Member register(RegisterMemberRequest registerMemberRequest) {
        checkEmailExist(registerMemberRequest);

        Member member = Member.register(registerMemberRequest, passwordEncode);

        memberRepository.save(member);

        sendEmail(member);

        return member;
    }

    @Override
    public Member activate(Long id) {
        Member member = memberRepository.findById(id).orElseThrow();

        if(member.getStatus() != MemberStatus.ACTIVE) {
            member.activate();
            memberRepository.save(member);
        }else {
            throw new IllegalStateException("Member is already active.");
        }

        return member;
    }

    @Override
    public Member deactivate(Long id) {
        Member member = memberRepository.findById(id).orElseThrow();

        if(member.getStatus() != MemberStatus.DEACTIVATED) {
            member.deactivate();
            memberRepository.save(member);
        }else {
            throw new IllegalStateException("Member is already deactivated.");
        }

        return member;                                                       }

    private void sendEmail(Member member) {
        emailSender.send(member.getEmail(), "이메일 발송", "이메일");
    }

    private void checkEmailExist(RegisterMemberRequest registerMemberRequest) {
        Optional<Member> byEmail = memberRepository.findByEmail(new Email(registerMemberRequest.email()));
        if(byEmail.isPresent()) {
            throw new DuplicateEmailException(registerMemberRequest.email());
        }
        ;
    }
}
