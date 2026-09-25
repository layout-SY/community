package tobyspring.community.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tobyspring.community.application.provided.MemberFinder;
import tobyspring.community.application.required.MemberRepository;
import tobyspring.community.domain.Member;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberQueryService implements MemberFinder {
    private final MemberRepository memberRepository;

    @Override
    public Member findById(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(
                () -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다 : " + memberId));
    }
}
