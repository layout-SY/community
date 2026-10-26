package tobyspring.community.adapter.webapi;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import tobyspring.community.adapter.webapi.post.PostResponseDto;
import tobyspring.community.application.member.MemberAuth;
import tobyspring.community.application.member.provided.MemberLogin;
import tobyspring.community.application.member.provided.MemberRegister;
import tobyspring.community.application.member.required.MemberRepository;
import tobyspring.community.application.post.required.PostRepository;
import tobyspring.community.domain.member.LoginResponseDto;
import tobyspring.community.domain.member.Member;
import tobyspring.community.domain.member.MemberFixture;
import tobyspring.community.domain.post.*;

import java.util.ArrayList;
import java.util.stream.Stream;

@SpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@AutoConfigureMockMvc
@Transactional
@Import(TestFixture.class)
public class PostControllerTest {
    private final MockMvcTester mvcTester;
    private final PostRepository postRepository;
    private final TestFixture testFixture;
    private final MemberRegister memberRegister;
    private final MemberLogin memberLogin;

    Member member;
    LoginResponseDto loginResponseDto;

    @BeforeEach
    void setUp() {
        member = memberRegister.register(MemberFixture.getRegisterRequest("qwer@naver.com"));
        member.activate();
        loginResponseDto = memberLogin.login(member.getEmail(), "qwe123");
    }

    @Nested
    @DisplayName("게시글 생성")
    class Posting {
        @Test
        void 성공() {
            Assertions.assertThat(loginResponseDto.token()).isNotNull();

            CreatePostRequest createPostRequest = PostFixture.getCreatePostRequest();

            PostResponseDto postResponseDto = createPostAndGetPostResponseDto(createPostRequest);

            Assertions.assertThat(postResponseDto.createdAt()).isNotNull();
            Assertions.assertThat(postResponseDto.postId()).isNotNull();
            Assertions.assertThat(postResponseDto.memberNickname()).isEqualTo(member.getNickname());
        }

        @Test
        void 성공_tags_누락_시_빈_목록() {
            CreatePostRequest createPostRequestTagsNull = new CreatePostRequest("title", "contentcontent", PostCategory.IT, null, PostFixture.getTags());

            PostResponseDto postResponseDto = createPostAndGetPostResponseDto(createPostRequestTagsNull);

            Assertions.assertThat(postResponseDto.tags()).isEmpty();
        }

        @Test
        void 성공_imageUrls_누락_시_빈_목록() {
            CreatePostRequest createPostRequestImageNull = new CreatePostRequest("titletitle", "contentcontent", PostCategory.IT, PostFixture.getTags(), null);

            PostResponseDto postResponseDto = createPostAndGetPostResponseDto(createPostRequestImageNull);

            Assertions.assertThat(postResponseDto.imageUrls()).isEmpty();
        }

        @Test
        void 성공_category_누락_시_ETC() {
            CreatePostRequest createPostRequestCategoryNull = new CreatePostRequest("title", "contentcontent", null, PostFixture.getTags(), PostFixture.getTags());

            PostResponseDto postResponseDto = createPostAndGetPostResponseDto(createPostRequestCategoryNull);

            Assertions.assertThat(postResponseDto.category()).isEqualTo(PostCategory.ETC);
        }

        @ParameterizedTest(name = "{0} 누락")
        @MethodSource("missingRequiredFields")
        void 실패_400_필수_파라미터_누락(String field, CreatePostRequest request) {
            testFixture.assertProblem(createPostApi(request, bearer(loginResponseDto.token().accessToken())),
                    HttpStatus.BAD_REQUEST, field + "은 필수 입력입니다. : null");
        }

        static Stream<Arguments> missingRequiredFields() {
            return Stream.of(
                    Arguments.of("title", new CreatePostRequest(null, "content", PostCategory.IT, null, null)),
                    Arguments.of("content", new CreatePostRequest("title", null, PostCategory.IT, null, null))
            );
        }

        @Test
        void 실패_401_유효한_토큰_아님() {
            testFixture.assertProblem(tokenValidator(bearer("invalid-token")), HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
        }

        @Test
        void 실패_401_토큰_헤더_없음() {
            testFixture.assertProblem(tokenValidator(bearer(null)), HttpStatus.UNAUTHORIZED, "Required header 'Authorization' is not present.");
        }

        @Test
        void 실패_401_유효하지_않은_헤더() {
            testFixture.assertProblem(tokenValidator(bearer("")), HttpStatus.UNAUTHORIZED, "Authorization 헤더가 올바르지 않습니다");
        }
    }

    @Nested
    @DisplayName("게시글 수정")
    class ModifyPost {

        PostResponseDto postResponseDto;

        @BeforeEach
        void setup() {
            CreatePostRequest createPostRequest = PostFixture.getCreatePostRequest();

            postResponseDto = createPostAndGetPostResponseDto(createPostRequest);

            Assertions.assertThat(
                    postResponseDto.postId()
            ).isNotNull();
        }

        @Test
        void 성공() {
            ModifyPostRequest modifyPostRequest = PostFixture.getModifyPostRequest();

            PostResponseDto modifiedPost = modifyPostAndGetPostResponseDto(modifyPostRequest);

            Assertions.assertThat(modifiedPost.modifiedAt()).isNotNull();
            Assertions.assertThat(modifiedPost.category()).isEqualTo(PostCategory.COOK);
        }

        @Test
        void 성공_파라미터_누락_시_기존_값_유지() {

        }
    }

    private PostResponseDto createPostAndGetPostResponseDto(CreatePostRequest createPostRequest) {
        MvcTestResult exchange = createPostApi(createPostRequest, bearer(loginResponseDto.token().accessToken()));

        Assertions.assertThat(exchange).hasStatusOk();

        return testFixture.readBody(exchange, PostResponseDto.class);
    }

    private PostResponseDto modifyPostAndGetPostResponseDto(ModifyPostRequest modifyPostRequest) {
        MvcTestResult exchange = modifyPostApi(modifyPostRequest, bearer(loginResponseDto.token().accessToken()));

        Assertions.assertThat(exchange).hasStatusOk();

        return testFixture.readBody(exchange, PostResponseDto.class);
    }

    private @NonNull MvcTestResult tokenValidator(String token) {
        CreatePostRequest createPostRequest = PostFixture.getCreatePostRequest();

        return createPostApi(createPostRequest, token);
    }

    private MvcTestResult createPostApi(CreatePostRequest createPostRequest, String token) {
        String requestBody = testFixture.toJson(createPostRequest);

        return mvcTester.post().uri("/post")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .exchange();
    }

    private MvcTestResult modifyPostApi(ModifyPostRequest modifyPostRequest, String token) {
        String requestBody = testFixture.toJson(modifyPostRequest);

        return mvcTester.put().uri("/post")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .exchange();
    }

    private static String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }
}
