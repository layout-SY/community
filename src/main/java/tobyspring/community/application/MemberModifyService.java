package tobyspring.community.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import tobyspring.community.application.provided.MemberFinder;
import tobyspring.community.application.provided.MemberRegister;
import tobyspring.community.application.required.EmailSender;
import tobyspring.community.application.required.MemberRepository;
import tobyspring.community.domain.member.PasswordEncode;
import tobyspring.community.domain.shared.Email;
import tobyspring.community.domain.member.Member;
import tobyspring.community.domain.member.MemberStatus;
import tobyspring.community.domain.member.RegisterMemberRequest;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Validated
public class MemberModifyService implements MemberRegister {
    private final MemberFinder memberFinder;
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
        Member member = memberFinder.findById(id);

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
        Member member = memberFinder.findById(id);

        if((member.getStatus() != MemberStatus.DEACTIVATED) && (member.getStatus() == MemberStatus.ACTIVE)) {
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
