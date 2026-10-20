package tobyspring.community.application.member.required;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import tobyspring.community.application.member.provided.MemberRegisterConfig;
import tobyspring.community.adapter.security.PasswordEncode;

@SpringBootTest
@Import(MemberRegisterConfig.class)
record PasswordEncoderTest(PasswordEncode passwordEncode) {

    @Test
    void 비밀번호_테스트() {
        String encode = passwordEncode.encode("qwer1234!");

        Assertions.assertThat(passwordEncode.match("qwer1234!", encode)).isTrue();
    }

    @Test
    void 비밀번호_테스트_실패() {
        String encode = passwordEncode.encode("qwer1234!");

        Assertions.assertThat(passwordEncode.match("qwer1234", encode)).isFalse();
    }
}
