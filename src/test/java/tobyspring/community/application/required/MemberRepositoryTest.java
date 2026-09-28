package tobyspring.community.application.required;

import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import tobyspring.community.domain.member.Member;
import tobyspring.community.domain.member.MemberFixture;
import tobyspring.community.domain.member.MemberStatus;
import tobyspring.community.adapter.security.PasswordEncode;

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

        Assertions.assertThat(memberById.getEmail().equals(this.member.getEmail())).isTrue();

        Assertions.assertThat(memberById.getDetail().getRegisteredAt()).isNotNull();
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

    @Test
    void 회원_id_조회() {
        Member saved = memberRepository.save(this.member);

        Member foundMember = memberRepository.findById(saved.getId()).orElseThrow();

        Assertions.assertThat(foundMember.getEmail()).isEqualTo(this.member.getEmail());
        Assertions.assertThat(foundMember.getStatus()).isEqualTo(MemberStatus.PENDING);
    }
}
