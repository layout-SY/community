package tobyspring.community.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tobyspring.community.domain.member.MemberDetailRequest;
import tobyspring.community.domain.member.Member;
import tobyspring.community.domain.member.MemberStatus;
import tobyspring.community.adapter.security.PasswordEncode;

import static tobyspring.community.domain.member.MemberFixture.createPasswordEncoder;
import static tobyspring.community.domain.member.MemberFixture.getRegisterRequest;

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
        Assertions.assertThat(member.getDetail().getRegisteredAt()).isNotNull();
    }

    @Test
    void 회원_등록완료() {
        member.activate();

        Assertions.assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        Assertions.assertThat(member.getDetail().getActivatedAt()).isNotNull();
    }

    @Test
    void 회원_탈퇴() {
        member.activate();

        Assertions.assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        Assertions.assertThat(member.getDetail().getActivatedAt()).isNotNull();
        Assertions.assertThat(member.getDetail().getDeactivatedAt()).isNull();

        member.deactivate();

        Assertions.assertThat(member.getStatus()).isEqualTo(MemberStatus.DEACTIVATED);
        Assertions.assertThat(member.getDetail().getDeactivatedAt()).isNotNull();
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

    @Test
    void 비밀번호_검증() {
        Assertions.assertThat(member.verifyPassword("qwe123", passwordEncode));
    }

    @Test
    void 비밀번호_틀림() {
        Assertions.assertThat(member.verifyPassword("qwer1234", passwordEncode)).isEqualTo(false);
    }

    @Test
    void 회원_정보_수정() {
        member.activate();

        Assertions.assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        Assertions.assertThat(member.getDetail().getActivatedAt()).isNotNull();

        Member updatedMember = member.updateInfo("qwe123", new MemberDetailRequest("qwwe", "자기소개"));

        Assertions.assertThat(updatedMember.getDetail().getIntroduction()).isEqualTo("자기소개");
        Assertions.assertThat(updatedMember.getDetail().getProfile().profile_address()).isEqualTo("qwwe");
    }

    @Test
    void 회원_정보_수정_실패_not_activate() {
        Assertions.assertThat(member.getStatus()).isEqualTo(MemberStatus.PENDING);

        Assertions.assertThatThrownBy(
                () -> member.updateInfo("qwe123", new MemberDetailRequest("qwwe", "자기소개")), "Member is not active");
    }
}
