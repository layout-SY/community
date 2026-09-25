package tobyspring.community.application.required;

import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import tobyspring.community.domain.Member;
import tobyspring.community.domain.MemberFixture;
import tobyspring.community.domain.MemberStatus;
import tobyspring.community.domain.PasswordEncode;

@DataJpaTest
class MemberRepositoryTest {
    Member member;
    PasswordEncode passwordEncode;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EntityManager entityManager;


    @BeforeEach
    void setUp() {
        this.passwordEncode = MemberFixture.createPasswordEncoder();
        this.member = Member.register(MemberFixture.getRegisterRequest(), passwordEncode);
    }

    @Test
    void 회원_생성() {
        Member saved = memberRepository.save(this.member);

        entityManager.flush();
//        entityManager.clear();

        Assertions.assertThat(saved.getEmail()).isEqualTo(this.member.getEmail());
        Assertions.assertThat(saved.getStatus()).isEqualTo(this.member.getStatus());

        Member memberById = memberRepository.findById(saved.getId()).orElseThrow();

        System.out.println(memberById);
        System.out.println(saved);

        Assertions.assertThat(memberById.getEmail().equals(this.member.getEmail())).isTrue();
    }

    @Test
    void 회원_생성_중복_이메일_실패() {
        Member saved = memberRepository.save(this.member);

        entityManager.flush();
        entityManager.clear();

        Assertions.assertThat(saved.getEmail()).isEqualTo(this.member.getEmail());
        Assertions.assertThat(saved.getStatus()).isEqualTo(MemberStatus.PENDING);

        Member member2 = Member.register(MemberFixture.getRegisterRequest(), passwordEncode);

        Assertions.assertThatThrownBy(
                () -> memberRepository.save(member2)
        ).isInstanceOf(DataIntegrityViolationException.class);
    }
}
