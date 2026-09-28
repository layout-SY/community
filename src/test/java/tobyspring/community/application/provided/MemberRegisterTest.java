package tobyspring.community.application.provided;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import tobyspring.community.application.DuplicateEmailException;
import tobyspring.community.application.required.MemberRepository;
import tobyspring.community.domain.member.*;

@SpringBootTest
@Transactional
@Import(MemberRegisterConfig.class)
record MemberRegisterTest(MemberRegister memberRegister, MemberRepository memberRepository, EntityManager entityManager) {

    @Test
    void 회원_등록() {
        Member member = memberRegister.register(MemberFixture.getRegisterRequest());

        Assertions.assertThat(member).isNotNull();
        Assertions.assertThat(member.getStatus()).isEqualTo(MemberStatus.PENDING);
    }

    @Test
    void 회원_등록_실패_invalidEmail() {
        memberRequestValidationFail(new RegisterMemberRequest("invalid email", "zxcv", "password"));
        memberRequestValidationFail(new RegisterMemberRequest("qwer1234@naver.com", "zxcvasdfqwer", "password"));
        memberRequestValidationFail(new RegisterMemberRequest("qwer1234@naver.com", "zxcv", "passwordpasswordpassword"));
    }

    private void memberRequestValidationFail(RegisterMemberRequest checker) {
        Assertions.assertThatThrownBy(() ->
                memberRegister.register(checker))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void 이메일_중복_실패() {
        memberRegister.register(MemberFixture.getRegisterRequest());

        Assertions.assertThatThrownBy(() -> memberRegister.register(MemberFixture.getRegisterRequest())).isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void 회원_activate_성공() {
        Member member = memberRegister.register(MemberFixture.getRegisterRequest());

        Assertions.assertThat(member.getStatus()).isEqualTo(MemberStatus.PENDING);

        entityManager.flush();
        entityManager.clear();

        memberRegister.activate(member.getId());

        Member foundMember = memberRepository.findById(member.getId()).orElseThrow();

        entityManager.flush();
        entityManager.clear();

        Assertions.assertThat(foundMember.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        Assertions.assertThat(foundMember.getDetail().getActivatedAt()).isNotNull();
    }

    @Test
    void 회원_deactivate_성공() {
        Member member = memberRegister.register(MemberFixture.getRegisterRequest());

        Assertions.assertThat(member.getStatus()).isEqualTo(MemberStatus.PENDING);

        entityManager.flush();
        entityManager.clear();

        memberRegister.activate(member.getId());

        memberRegister.deactivate(member.getId());

        Member foundMember = memberRepository.findById(member.getId()).orElseThrow();

        entityManager.flush();
        entityManager.clear();

        Assertions.assertThat(foundMember.getStatus()).isEqualTo(MemberStatus.DEACTIVATED);
        Assertions.assertThat(foundMember.getDetail().getDeactivatedAt()).isNotNull();
    }

    @Test
    void 회원_deactivate_실패_activate_아님() {
        Member member = memberRegister.register(MemberFixture.getRegisterRequest());

        Assertions.assertThat(member.getStatus()).isEqualTo(MemberStatus.PENDING);

        entityManager.flush();
        entityManager.clear();

        Assertions.assertThatThrownBy(
                () -> memberRegister.deactivate(member.getId())).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 회원_정보_수정() {
        Member member = memberRegister.register(MemberFixture.getRegisterRequest());

        Assertions.assertThat(member.getStatus()).isEqualTo(MemberStatus.PENDING);

        entityManager.flush();

        Member activtedMember = memberRegister.activate(member.getId());
        Assertions.assertThat(activtedMember.getStatus()).isEqualTo(MemberStatus.ACTIVE);

        memberRegister.updateInfo(member.getId(), "qwer1234", MemberFixture.getMemberDetailRequest("asdf"));
        Member foundMember = memberRepository.findById(member.getId()).orElseThrow();

        entityManager.flush();

        Assertions.assertThat(foundMember.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        Assertions.assertThat(foundMember.getDetail().getProfile().profile_address()).isEqualTo("asdf");
    }
}