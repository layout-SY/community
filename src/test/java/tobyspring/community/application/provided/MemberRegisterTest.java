package tobyspring.community.application.provided;

import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import tobyspring.community.application.DuplicateEmailException;
import tobyspring.community.domain.Member;
import tobyspring.community.domain.MemberFixture;
import tobyspring.community.domain.MemberStatus;
import tobyspring.community.domain.RegisterMemberRequest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Import(MemberRegisterConfig.class)
record MemberRegisterTest(MemberRegister memberRegister) {

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
}