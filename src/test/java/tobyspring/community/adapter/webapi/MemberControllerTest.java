package tobyspring.community.adapter.webapi;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import tobyspring.community.TestFixture;
import tobyspring.community.adapter.security.TokenManager;
import tobyspring.community.adapter.webapi.MemberDto;
import tobyspring.community.application.required.MemberRepository;
import tobyspring.community.domain.member.*;
import tobyspring.community.domain.shared.Email;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@AutoConfigureMockMvc
@Transactional
@Import(TestFixture.class)
public class MemberControllerTest {
    private final MockMvcTester mvcTester;
    private final MemberRepository memberRepository;
    private final TokenManager tokenManager;
    private final TestFixture testFixture;

    @Nested
    @DisplayName("회원 생성")
    class Registration {
        @Test
        void 성공() {
            MemberDto registeredMember = registerMember(MemberFixture.getRegisterRequest());

            Member member = memberRepository.findById(registeredMember.id()).orElseThrow();

            assertThat(registeredMember.status()).isEqualTo(MemberStatus.PENDING);
            assertThat(registeredMember.detail().getRegisteredAt()).isNotNull();

            assertThat(member.getStatus()).isEqualTo(MemberStatus.PENDING);
            assertThat(member.getDetail().getRegisteredAt()).isNotNull();
        }

        @Test
        void 실패_400_body_없음() {
            MvcTestResult exchange = mvcTester.post().uri("/member/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .exchange();

            assertThat(exchange).hasStatus(HttpStatus.BAD_REQUEST);
        }

        @Test
        void 실패_400_body_email_없음() {
            MvcTestResult exchange = register(testFixture.toJson(Map.of("nickname", "qwer123", "password", "zxcv123!")));

            assertThat(exchange).hasStatus(HttpStatus.BAD_REQUEST);
        }

        @Test
        void 실패_409_email_중복() {
            RegisterMemberRequest request = MemberFixture.getRegisterRequest();
            registerMember(request);

            MvcTestResult exchange = register(testFixture.toJson(request));

            testFixture.assertProblem(exchange, HttpStatus.CONFLICT, "이미 존재하는 이메일 입니다 : " + request.email());
        }

        @Test
        void 실패_400_email_형식_아님() {
            MvcTestResult exchange = register(testFixture.toJson(MemberFixture.getRegisterRequest("invalid email")));

            assertThat(exchange).hasStatus(HttpStatus.BAD_REQUEST);
            assertThat(testFixture.readBody(exchange, ProblemDetail.class).getDetail()).isNotNull();
        }

        @Test
        void 실패_400_잘못된_JSON() {
            MvcTestResult exchange = register("{");

            assertThat(exchange).hasStatus(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("회원 활성화")
    class Activation {
        @Test
        void 성공() {
            AuthenticatedMember pending = registerAndLogin();

            MvcTestResult exchange = activate(pending.accessToken());

            assertThat(exchange).hasStatus(HttpStatus.OK);

            MemberDto activated = testFixture.readBody(exchange, MemberDto.class);

            assertThat(activated.status()).isEqualTo(MemberStatus.ACTIVE);
            assertThat(activated.detail().getActivatedAt()).isNotNull();
        }

        @Test
        void 실패_400_이미_activate() {
            AuthenticatedMember active = activeMember();

            MvcTestResult exchange = activate(active.accessToken());

            testFixture.assertProblem(exchange, HttpStatus.BAD_REQUEST, "Member is already active.");
        }

        @Test
        void 실패_401_유효하지_않은_토큰_정보() {
            MvcTestResult exchange = activate("invalid-token");

            assertThat(exchange).hasStatus(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("회원 비활성화")
    class Deactivation {
        @Test
        void 성공() {
            AuthenticatedMember active = activeMember();

            MvcTestResult exchange = deactivate(active.accessToken());

            assertThat(exchange).hasStatus(HttpStatus.OK);

            MemberDto deactivated = testFixture.readBody(exchange, MemberDto.class);

            assertThat(deactivated.status()).isEqualTo(MemberStatus.DEACTIVATED);
            assertThat(deactivated.detail().getDeactivatedAt()).isNotNull();
        }

        @Test
        void 실패_400_이미_deactivate() {
            AuthenticatedMember deactivated = deactivatedMember();

            MvcTestResult exchange = deactivate(deactivated.accessToken());

            testFixture.assertProblem(exchange, HttpStatus.BAD_REQUEST, "Member is already deactivate.");
        }
    }

    @Nested
    @DisplayName("회원 정보 수정")
    class ProfileUpdate {
        @Test
        void 성공() {
            AuthenticatedMember active = activeMember();

            MvcTestResult exchange = update(active.accessToken(), "qwer123", MemberFixture.getMemberDetailRequest());

            assertThat(exchange).hasStatus(HttpStatus.OK);

            MemberDto updated = testFixture.readBody(exchange, MemberDto.class);

            assertThat(updated.status()).isEqualTo(MemberStatus.ACTIVE);
            assertThat(updated.detail().getProfile().profile_address()).isEqualTo("qwerasdf");
            assertThat(updated.nickname()).isEqualTo("qwer123");
        }

        @Test
        void 실패_400_pending_상태에서_변경() {
            AuthenticatedMember pending = registerAndLogin();

            MvcTestResult exchange = update(pending.accessToken(), "qwer123", MemberFixture.getMemberDetailRequest());

            assertThat(exchange).hasStatus(HttpStatus.BAD_REQUEST);
        }

        @Test
        void 실패_400_deactivate_상태에서_변경() {
            AuthenticatedMember deactivated = deactivatedMember();

            MvcTestResult exchange = update(deactivated.accessToken(), "qwer123", MemberFixture.getMemberDetailRequest());

            assertThat(exchange).hasStatus(HttpStatus.BAD_REQUEST);
        }

        @Test
        void 실패_400_nickname_파라미터_없음() {
            AuthenticatedMember active = activeMember();

            MvcTestResult exchange = mvcTester.put().uri("/update")
                    .header(HttpHeaders.AUTHORIZATION, bearer(active.accessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(testFixture.toJson(MemberFixture.getMemberDetailRequest()))
                    .exchange();

            assertThat(exchange).hasStatus(HttpStatus.BAD_REQUEST);
        }

        @Test
        void 실패_detail_body_없음() {
            AuthenticatedMember active = activeMember();

            MvcTestResult exchange = mvcTester.put().uri("/update")
                    .header(HttpHeaders.AUTHORIZATION, bearer(active.accessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .exchange();

            assertThat(exchange).hasStatus(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("회원id로 조회")
    class getMember {
        @Test
        void 성공() {
            MemberDto memberDto = registerMember(MemberFixture.getRegisterRequest());

            MvcTestResult exchange = getMember(memberDto);

            MemberDto memberResponse = testFixture.readBody(exchange, MemberDto.class);

            assertThat(exchange).hasStatus(HttpStatus.OK);
            assertThat(memberResponse.id()).isEqualTo(memberDto.id());
        }



        @Test
        void 실패_404_없는_member() {
            MemberDto memberDto = registerMember(MemberFixture.getRegisterRequest());

            MvcTestResult exchange = mvcTester.get().uri("/member/" + 1234)
                    .contentType(MediaType.APPLICATION_JSON)
                    .exchange();

            ProblemDetail problemDetail = testFixture.readBody(exchange, ProblemDetail.class);

            assertThat(exchange).hasStatus(HttpStatus.NOT_FOUND);
            assertThat(problemDetail.getDetail()).isEqualTo("회원 정보가 없습니다. : " + 1234);
        }

        @Test
        void 실패_404_문자가_들어간_memberId() {
            MemberDto memberDto = registerMember(MemberFixture.getRegisterRequest());

            MvcTestResult exchange = mvcTester.get().uri("/member/" + "1234abc")
                    .contentType(MediaType.APPLICATION_JSON)
                    .exchange();

            ProblemDetail problemDetail = testFixture.readBody(exchange, ProblemDetail.class);

            assertThat(exchange).hasStatus(HttpStatus.BAD_REQUEST);
            assertThat(problemDetail.getDetail()).isEqualTo("Failed to convert 'id' with value: '1234abc'");
        }
    }

    @Nested
    @DisplayName("로그인")
    class Login {
        @Test
        void 성공() {
            RegisterMemberRequest request = MemberFixture.getRegisterRequest();
            MemberDto registeredMember = registerMember(request);

            MvcTestResult exchange = login(request.email(), request.password());

            assertThat(exchange).hasStatus(HttpStatus.OK);

            LoginResponseDto loginResponse = testFixture.readBody(exchange, LoginResponseDto.class);

            assertThat(loginResponse.member().getEmail().getAddress()).isEqualTo(registeredMember.email());
            assertThat(tokenManager.extractEmailFromToken(loginResponse.token().accessToken()))
                    .isEqualTo(registeredMember.email());
        }
    }

    // 사전 조건 준비

    private record AuthenticatedMember(MemberDto member, String accessToken) {
    }

    private MemberDto registerMember(RegisterMemberRequest request) {
        MvcTestResult exchange = register(testFixture.toJson(request));

        assertThat(exchange).hasStatus(HttpStatus.OK);

        return testFixture.readBody(exchange, MemberDto.class);
    }

    private AuthenticatedMember registerAndLogin() {
        RegisterMemberRequest request = MemberFixture.getRegisterRequest();
        MemberDto member = registerMember(request);

        MvcTestResult exchange = login(request.email(), request.password());

        assertThat(exchange).hasStatus(HttpStatus.OK);

        return new AuthenticatedMember(member, testFixture.readBody(exchange, LoginResponseDto.class).token().accessToken());
    }

    private AuthenticatedMember activeMember() {
        AuthenticatedMember pending = registerAndLogin();

        MvcTestResult exchange = activate(pending.accessToken());

        assertThat(exchange).hasStatus(HttpStatus.OK);

        return new AuthenticatedMember(testFixture.readBody(exchange, MemberDto.class), pending.accessToken());
    }

    private AuthenticatedMember deactivatedMember() {
        AuthenticatedMember active = activeMember();

        MvcTestResult exchange = deactivate(active.accessToken());

        assertThat(exchange).hasStatus(HttpStatus.OK);

        return new AuthenticatedMember(testFixture.readBody(exchange, MemberDto.class), active.accessToken());
    }

    // API 호출

    private MvcTestResult register(String json) {
        return mvcTester.post().uri("/member/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .exchange();
    }

    private MvcTestResult login(String email, String password) {
        return mvcTester.post().uri("/member/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(testFixture.toJson(MemberFixture.getLoginRequest(new Email(email), password)))
                .exchange();
    }

    private MvcTestResult activate(String accessToken) {
        return mvcTester.get().uri("/activate")
                .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                .exchange();
    }

    private MvcTestResult deactivate(String accessToken) {
        return mvcTester.get().uri("/deactivate")
                .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                .exchange();
    }

    private MvcTestResult update(String accessToken, String nickname, MemberDetailRequest detailRequest) {
        return mvcTester.put().uri("/update")
                .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .param("nickname", nickname)
                .content(testFixture.toJson(detailRequest))
                .exchange();
    }

    private @NonNull MvcTestResult getMember(MemberDto memberDto) {
        return mvcTester.get().uri("/member/" + memberDto.id())
                .contentType(MediaType.APPLICATION_JSON)
                .exchange();
    }

    private static String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }


}
