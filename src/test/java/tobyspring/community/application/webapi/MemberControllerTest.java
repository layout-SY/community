package tobyspring.community.application.webapi;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import tobyspring.community.adapter.security.PasswordEncode;
import tobyspring.community.adapter.webapi.MemberController;
import tobyspring.community.adapter.webapi.MemberDto;
import tobyspring.community.application.provided.MemberRegister;
import tobyspring.community.domain.member.Member;
import tobyspring.community.domain.member.MemberFixture;
import tobyspring.community.domain.member.MemberStatus;
import tobyspring.community.domain.member.RegisterMemberRequest;
import tools.jackson.databind.ObjectMapper;

import java.io.UnsupportedEncodingException;

@SpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
//@WebMvcTest(MemberController.class)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MemberControllerTest {
    private final MockMvcTester mvcTester;
    private final ObjectMapper objectMapper;

    private MemberDto sharedMember;

    @BeforeEach
    void setUp() {
    }

    @Test
    @Order(1)
    void 회원_생성_api() throws UnsupportedEncodingException {
        this.sharedMember = registerMemberApi();

        Assertions.assertThat(sharedMember.status()).isEqualTo(MemberStatus.PENDING);
        Assertions.assertThat(sharedMember.detail().getRegisteredAt()).isNotNull();
    }

    @Test
    @Order(2)
    void 회원_activate_api() throws UnsupportedEncodingException {
        MvcTestResult exchange = mvcTester.get().uri("/activate/" + this.sharedMember.id())
                .contentType(MediaType.APPLICATION_JSON)
                .exchange();

        MemberDto activatedMemberDto = objectMapper.readValue(exchange.getResponse().getContentAsString(), MemberDto.class);

        Assertions.assertThat(activatedMemberDto.status()).isEqualTo(MemberStatus.ACTIVE);
        Assertions.assertThat(activatedMemberDto.detail().getActivatedAt()).isNotNull();

        this.sharedMember = activatedMemberDto;
    }

    @Test
    @Order(4)
    void 회원_deactivate_api() throws UnsupportedEncodingException {

        MvcTestResult exchange = mvcTester.get().uri("/deactivate/" + this.sharedMember.id())
                .contentType(MediaType.APPLICATION_JSON)
                .exchange();

        MemberDto deactivatedMemberDto = objectMapper.readValue(exchange.getResponse().getContentAsString(), MemberDto.class);

        Assertions.assertThat(deactivatedMemberDto.status()).isEqualTo(MemberStatus.DEACTIVATED);
        Assertions.assertThat(deactivatedMemberDto.detail().getDeactivatedAt()).isNotNull();

        this.sharedMember = deactivatedMemberDto;
    }

    @Test
    @Order(3)
    void 회원_정보_수정_api() throws UnsupportedEncodingException {
        MvcTestResult exchange = mvcTester.put().uri("/update/" + this.sharedMember.id())
                .contentType(MediaType.APPLICATION_JSON)
                .param("nickname", "qwer123")
                .content(objectMapper.writeValueAsString(MemberFixture.getMemberDetailRequest()))
                .exchange();

        MemberDto updatedMemberDto = objectMapper.readValue(exchange.getResponse().getContentAsString(), MemberDto.class);

        Assertions.assertThat(updatedMemberDto.status()).isEqualTo(MemberStatus.ACTIVE);
        Assertions.assertThat(updatedMemberDto.detail().getProfile().profile_address()).isEqualTo("qwerasdf");
        Assertions.assertThat(updatedMemberDto.nickname()).isEqualTo("qwer123");

        this.sharedMember =  updatedMemberDto;
    }

    private @NonNull MemberDto registerMemberApi() throws UnsupportedEncodingException {
        RegisterMemberRequest request = MemberFixture.getRegisterRequest();

        MvcTestResult exchange = mvcTester.post().uri("/member/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        return objectMapper.readValue(exchange.getResponse().getContentAsString(), MemberDto.class);
    }
}
