package tobyspring.community.application.member;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tobyspring.community.application.member.exception.NotFoundException;
import tobyspring.community.application.member.provided.MemberFinder;
import tobyspring.community.application.member.required.MemberRepository;
import tobyspring.community.domain.member.Member;
import tobyspring.community.domain.shared.Email;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberQueryService implements MemberFinder {
    private final MemberRepository memberRepository;

    @Override
    public Member findById(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(
                () -> new NotFoundException("회원 정보가 없습니다. : " + memberId));
    }

    @Override
    public Member findByEmail(Email email) {
        return memberRepository.findByEmail(email).orElseThrow(
                () -> new NotFoundException("회원 정보가 없습니다 : " + email)
        );
    }
}
