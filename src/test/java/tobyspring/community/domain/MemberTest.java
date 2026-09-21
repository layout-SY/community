package tobyspring.community.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static tobyspring.community.domain.MemberFixture.createPasswordEncoder;
import static tobyspring.community.domain.MemberFixture.getRegisterRequest;

public class MemberTest {
    private Member member;
    private PasswordEncode passwordEncode;

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
        Assertions.assertThat(passwordEncode.match("qwer1234!", member.getPasswordHash())).isTrue();
    }

    @Test
    void 회원_비밀번호_수정_실패_pending() {
        Assertions.assertThatThrownBy(() -> member.changePassword("qwer1234!", passwordEncode),
                "Member is not active"
        );
    }
}
