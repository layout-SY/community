package tobyspring.community.application.member.required;

import org.springframework.data.repository.Repository;
import tobyspring.community.domain.shared.Email;
import tobyspring.community.domain.member.Member;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends Repository<Member, Long> {
    Member save(Member member);

    Optional<Member> findByEmail(Email email);

    Optional<Member> findById(Long id);

    List<Member> findAllByIdIn(Collection<Long> ids);
}
