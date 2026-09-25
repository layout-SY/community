package tobyspring.community.application.required;

import org.springframework.data.repository.Repository;
import tobyspring.community.domain.Email;
import tobyspring.community.domain.Member;

import java.util.Optional;

public interface MemberRepository extends Repository<Member, Long> {
    Member save(Member member);

    Optional<Member> findByEmail(Email email);

    Optional<Member> findById(Long id);
}
