package tobyspring.community.application.required;

import org.springframework.data.repository.Repository;
import tobyspring.community.domain.Member;

public interface MemberRepository extends Repository<Member, Long> {
    Member save(Member member);
}
