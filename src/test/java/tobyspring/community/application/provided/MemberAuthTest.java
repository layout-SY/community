package tobyspring.community.application.provided;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import tobyspring.community.TestFixture;
import tobyspring.community.adapter.webapi.MemberDto;
import tobyspring.community.application.DeactivateMemberException;
import tobyspring.community.application.NotFoundException;
import tobyspring.community.application.required.MemberRepository;
import tobyspring.community.domain.member.*;
import tobyspring.community.domain.shared.Email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@RequiredArgsConstructor
@Transactional
public class MemberAuthTest {
    private final MemberLogin memberLogin;
    private final MemberRepository memberRepository;
    private final MemberRegister memberRegister;

    @Nested
    @DisplayName("로그인")
    class 로그인 {
        @Test
        void 로그인_성공() {
            Member member = memberRegister.register(MemberFixture.getRegisterRequest());

            LoginResponseDto logined = memberLogin.login(member.getEmail(), "qwe123");

            assertThat(member).isNotNull();
            assertThat(member.getEmail().getAddress()).isEqualTo(logined.member().getEmail().getAddress());
            assertThat(logined.token().accessToken()).isNotNull();
        }

        @Test
        void 실패_400_존재하지_않는_계정() {
            Member member = memberRegister.register(MemberFixture.getRegisterRequest());

            Member found = memberRepository.findById(member.getId()).orElseThrow();

            assertThat(found.getStatus()).isEqualTo(MemberStatus.PENDING);

            assertThatThrownBy(
                    () -> memberLogin.login(new Email("qwerafd2@naver.com"), "qwe123")
            ).isInstanceOf(NotFoundException.class);
        }

        @Test
        void 실패_404_비밀번호_미일치() {
            Member member = memberRegister.register(MemberFixture.getRegisterRequest());

            assertThatThrownBy(
                    () -> memberLogin.login(member.getEmail(), "qwe1234")
            ).isInstanceOf(NotFoundException.class);
        }

        @Test
        void 실패_400_deactivated_member() {
            Member member = memberRegister.register(MemberFixture.getRegisterRequest());

            Member found = memberRepository.findById(member.getId()).orElseThrow();

            assertThat(found.getStatus()).isEqualTo(MemberStatus.PENDING);

            Member deactivated = memberRegister.deactivate(member.getId());

            assertThat(deactivated.getStatus()).isEqualTo(MemberStatus.DEACTIVATED);

            assertThatThrownBy(
                    () -> memberLogin.login(member.getEmail(), "qwe1234")
            ).isInstanceOf(DeactivateMemberException.class);
        }
    }
}
