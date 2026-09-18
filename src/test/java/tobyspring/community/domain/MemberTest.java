package tobyspring.community.domain;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import tobyspring.community.application.provided.Email;
import tobyspring.community.application.required.PasswordEncode;
import tobyspring.community.application.required.PasswordEncodeImpl;

import static tobyspring.community.domain.MemberFixture.createPasswordEncoder;
import static tobyspring.community.domain.MemberFixture.getRegisterRequest;

public class MemberTest {
    private Member member;
    private PasswordEncodeImpl passwordEncode;

    @BeforeEach
    public void setUp() {
        passwordEncode = createPasswordEncoder();
        member = Member.register(getRegisterRequest(), passwordEncode);
    }

    @Test
    void 회원_등록() {
        Assertions.assertThat(member.getNickname()).isNotNull();
    }

    @Test
    void 회원_등록완료() {
        member.activate();

        Assertions.assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
    }

    @Test
    void 회원_탈퇴() {
        member.activate();

        Assertions.assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);

        member.deactivate();

        Assertions.assertThat(member.getStatus()).isEqualTo(MemberStatus.DEACTIVATED);
    }

    @Test
    void 회원_비밀번호_수정() {
        member.activate();

        member.changePassword("qwer1234!", passwordEncode);

        Assertions.assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        Assertions.assertThat(member.getPasswordHash()).isEqualTo(passwordEncode.encode("qwer1234!"));
    }

    @Test
    void 회원_비밀번호_수정_실패_pending() {
        Assertions.assertThatThrownBy(() -> member.changePassword("qwer1234!", passwordEncode),
                "Member is not active"
        );
    }
}
