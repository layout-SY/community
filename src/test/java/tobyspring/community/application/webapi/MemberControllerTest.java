package tobyspring.community.application.webapi;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import tobyspring.community.adapter.webapi.MemberDto;
import tobyspring.community.application.required.MemberRepository;
import tobyspring.community.domain.member.Member;
import tobyspring.community.domain.member.MemberFixture;
import tobyspring.community.domain.member.MemberStatus;
import tobyspring.community.domain.member.RegisterMemberRequest;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@SpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@AutoConfigureMockMvc
@Transactional
public class MemberControllerTest {
    private final MockMvcTester mvcTester;
    private final ObjectMapper objectMapper;
    private final MemberRepository memberRepository;

    @Nested
    @DisplayName("회원 생성")
    class Registration {
        @Test
        void 성공() {
            MemberDto registeredMember = registerMemberApi();

            Member member = memberRepository.findById(registeredMember.id()).orElseThrow();

            Assertions.assertThat(registeredMember.status()).isEqualTo(MemberStatus.PENDING);
            Assertions.assertThat(registeredMember.detail().getRegisteredAt()).isNotNull();

            Assertions.assertThat(member.getStatus()).isEqualTo(MemberStatus.PENDING);
            Assertions.assertThat(member.getDetail().getRegisteredAt()).isNotNull();
        }

        @Test
        void 실패_400_body_없음() {
            MvcTestResult exchange = mvcTester.post().uri("/member/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .exchange();

            Assertions.assertThat(exchange)
                    .hasStatus(HttpStatus.BAD_REQUEST);
        }

        @Test
        void 실패_400_body_email_없음() {
            MvcTestResult exchange = mvcTester.post().uri("/member/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                            Map.of("nickname", "qwer123", "password", "zxcv123!")))
                    .exchange();

            Assertions.assertThat(exchange)
                    .hasStatus(HttpStatus.BAD_REQUEST);
        }

        @Test
        void 실패_409_email_중복() {
            registerMemberApi();
            RegisterMemberRequest request = MemberFixture.getRegisterRequest();

            MvcTestResult exchange = mvcTester.post().uri("/member/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .exchange();

            Assertions.assertThat(exchange)
                    .hasStatus(HttpStatus.CONFLICT);

            ProblemDetail problemDetail = objectMapper.readValue(exchange.getResponse().getContentAsByteArray(), ProblemDetail.class);

            Assertions.assertThat(problemDetail.getDetail()).isEqualTo("이미 존재하는 이메일 입니다 : " + request.email());
        }

        @Test
        void 실패_400_email_형식_아님() {
            RegisterMemberRequest request = MemberFixture.getRegisterRequest("invalid email");

            MvcTestResult exchange = mvcTester.post().uri("/member/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .exchange();

            Assertions.assertThat(exchange)
                    .hasStatus(HttpStatus.BAD_REQUEST);

            ProblemDetail problemDetail = objectMapper.readValue(exchange.getResponse().getContentAsByteArray(), ProblemDetail.class);

            Assertions.assertThat(problemDetail.getDetail()).isNotNull();
        }

        @Test
        void 실패_400_잘못된_JSON() {
            MvcTestResult exchange = mvcTester.post().uri("/member/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{")
                    .exchange();

            Assertions.assertThat(exchange).hasStatus(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("회원 활성화")
    class Activation {
        @Test
        void 회원_activate_api() {
            MemberDto registeredMember = registerMemberApi();
            MemberDto activatedMemberDto = activateMemberApi(registeredMember);

            Assertions.assertThat(activatedMemberDto.status()).isEqualTo(MemberStatus.ACTIVE);
            Assertions.assertThat(activatedMemberDto.detail().getActivatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("회원 비활성화")
    class Deactivation {
        @Test
        void 회원_deactivate_api() {
            MemberDto activeMember = activateMemberApi(registerMemberApi());

            MvcTestResult exchange = mvcTester.get().uri("/deactivate/" + activeMember.id())
                    .contentType(MediaType.APPLICATION_JSON)
                    .exchange();

            Assertions.assertThat(exchange).hasStatus(HttpStatus.OK);

            MemberDto deactivatedMemberDto = objectMapper.readValue(exchange.getResponse().getContentAsByteArray(), MemberDto.class);

            Assertions.assertThat(deactivatedMemberDto.status()).isEqualTo(MemberStatus.DEACTIVATED);
            Assertions.assertThat(deactivatedMemberDto.detail().getDeactivatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("회원 정보 수정")
    class ProfileUpdate {
        @Test
        void 회원_정보_수정_api() {
            MemberDto activeMember = activateMemberApi(registerMemberApi());

            MvcTestResult exchange = mvcTester.put().uri("/update/" + activeMember.id())
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("nickname", "qwer123")
                    .content(objectMapper.writeValueAsString(MemberFixture.getMemberDetailRequest()))
                    .exchange();

            Assertions.assertThat(exchange).hasStatus(HttpStatus.OK);

            MemberDto updatedMemberDto = objectMapper.readValue(exchange.getResponse().getContentAsByteArray(), MemberDto.class);

            Assertions.assertThat(updatedMemberDto.status()).isEqualTo(MemberStatus.ACTIVE);
            Assertions.assertThat(updatedMemberDto.detail().getProfile().profile_address()).isEqualTo("qwerasdf");
            Assertions.assertThat(updatedMemberDto.nickname()).isEqualTo("qwer123");
        }
    }


    private @NonNull MemberDto registerMemberApi() {
        RegisterMemberRequest request = MemberFixture.getRegisterRequest();

        MvcTestResult exchange = mvcTester.post().uri("/member/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        Assertions.assertThat(exchange).hasStatus(HttpStatus.OK);

        return objectMapper.readValue(exchange.getResponse().getContentAsByteArray(), MemberDto.class);
    }

    private @NonNull MemberDto activateMemberApi(MemberDto member) {
        MvcTestResult exchange = mvcTester.get().uri("/activate/" + member.id())
                .contentType(MediaType.APPLICATION_JSON)
                .exchange();

        Assertions.assertThat(exchange).hasStatus(HttpStatus.OK);

        return objectMapper.readValue(exchange.getResponse().getContentAsByteArray(), MemberDto.class);
    }
}
