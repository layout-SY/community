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
import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import tobyspring.community.TestFixture;
import tobyspring.community.adapter.security.TokenManager;
import tobyspring.community.adapter.webapi.post.PostResponseDto;
import tobyspring.community.application.member.MemberAuth;
import tobyspring.community.application.member.provided.MemberLogin;
import tobyspring.community.application.member.provided.MemberRegister;
import tobyspring.community.application.member.required.MemberRepository;
import tobyspring.community.application.post.provided.PostFinder;
import tobyspring.community.application.post.required.PostRepository;
import tobyspring.community.domain.member.LoginResponseDto;
import tobyspring.community.domain.member.Member;
import tobyspring.community.domain.member.MemberFixture;
import tobyspring.community.domain.post.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;

@SpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@AutoConfigureMockMvc
@Transactional
@Import(TestFixture.class)
class PostControllerTest {
    private final MockMvcTester mvcTester;
    private final TestFixture testFixture;
    private final MemberRegister memberRegister;
    private final MemberLogin memberLogin;

    private Member member;
    private String access_token;
    private static final String INVALID_PATH_ID = "invalid-post-id";

    private PostResponseDto postResponseDto;

    @BeforeEach
    void setUp() {
        member = memberRegister.register(MemberFixture.getRegisterRequest("qwer@naver.com"));
        member.activate();
        LoginResponseDto loginResponseDto = memberLogin.login(member.getEmail(), MemberFixture.DEFAULT_PASSWORD);
        access_token = loginResponseDto.token().accessToken();

        Assertions.assertThat(loginResponseDto.token().accessToken()).isNotNull();

    }

    @Nested
    @DisplayName("게시글 생성")
    class Posting {
        @Test
        void 성공() {
            CreatePostRequest createPostRequest = PostFixture.getCreatePostRequest();

            PostResponseDto postResponseDto = createAndToResponse(createPostRequest);

            Assertions.assertThat(postResponseDto.createdAt()).isNotNull();
            Assertions.assertThat(postResponseDto.postId()).isNotNull();
            Assertions.assertThat(postResponseDto.memberNickname()).isEqualTo(member.getNickname());
        }

        @Test
        void 성공_tags_누락_시_빈_목록() {
            CreatePostRequest createPostRequestTagsNull = new CreatePostRequest("title", "contentcontent", PostCategory.IT, null, PostFixture.getTags());

            PostResponseDto postResponseDto = createAndToResponse(createPostRequestTagsNull);

            Assertions.assertThat(postResponseDto.tags()).isEmpty();
        }

        @Test
        void 성공_imageUrls_누락_시_빈_목록() {
            CreatePostRequest createPostRequestImageNull = new CreatePostRequest("titletitle", "contentcontent", PostCategory.IT, PostFixture.getTags(), null);

            PostResponseDto postResponseDto = createAndToResponse(createPostRequestImageNull);

            Assertions.assertThat(postResponseDto.imageUrls()).isEmpty();
        }

        @Test
        void 성공_category_누락_시_ETC() {
            CreatePostRequest createPostRequestCategoryNull = new CreatePostRequest("title", "contentcontent", null, PostFixture.getTags(), PostFixture.getTags());

            PostResponseDto postResponseDto = createAndToResponse(createPostRequestCategoryNull);

            Assertions.assertThat(postResponseDto.category()).isEqualTo(PostCategory.ETC);
        }

        @ParameterizedTest(name = "{0} 누락")
        @MethodSource("missingRequiredFields")
        void 실패_400_필수_파라미터_누락(String field, CreatePostRequest request) {
            testFixture.assertProblem(createPostApi(request, bearer(access_token)),
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
        void 실패_400_토큰_헤더_없음() {
            testFixture.assertProblem(nonAuthenticationHeader(), HttpStatus.BAD_REQUEST, "Required header 'Authorization' is not present.");
        }

        private MvcTestResult nonAuthenticationHeader() {
            String requestBody = testFixture.toJson(PostFixture.getCreatePostRequest());

            return mvcTester.post().uri("/post")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .exchange();
        }

        @Test
        void 실패_401_유효하지_않은_헤더() {
            testFixture.assertProblem(tokenValidator(bearer("")), HttpStatus.UNAUTHORIZED, "Authorization 헤더가 올바르지 않습니다");
        }

        private @NonNull MvcTestResult tokenValidator(String token) {
            CreatePostRequest createPostRequest = PostFixture.getCreatePostRequest();

            return createPostApi(createPostRequest, token);
        }
    }

    @Nested
    @DisplayName("게시글 수정")
    class ModifyPost {
        @BeforeEach
        void setup() {
            createAndToResponse(PostFixture.getCreatePostRequest());
        }

        @Test
        void 성공() {
            ModifyPostRequest modifyPostRequest = PostFixture.getModifyPostRequest();

            PostResponseDto modifiedPost = modifyAndToResponse(postResponseDto.postId(), modifyPostRequest);

            Assertions.assertThat(modifiedPost.modifiedAt()).isNotNull();
            Assertions.assertThat(modifiedPost.category()).isEqualTo(PostCategory.COOK);
        }

        @Test
        void 성공_파라미터_누락_시_기존_값_유지() {
            PostResponseDto modified = modifyAndToResponse(postResponseDto.postId(), new ModifyPostRequest("title check", null, PostCategory.VACATION, PostFixture.getTags("djdjdj", "didi"), null));

            Assertions.assertThat(modified)
                    .extracting(PostResponseDto::title, PostResponseDto::content, PostResponseDto::category)
                    .containsExactly("title check", "본문 내용입니다.", PostCategory.VACATION);

            Assertions.assertThat(modified.tags())
                    .containsExactly("djdjdj", "didi");

            Assertions.assertThat(modified.imageUrls()).containsAll(postResponseDto.imageUrls());
        }

        @Test
        void 실패_400_필수_파라미터_없음() {
            MvcTestResult exchange = modifyPostApi(postResponseDto.postId(), Map.of(), bearer(access_token));

            testFixture.assertProblem(exchange, HttpStatus.BAD_REQUEST, "누락된 필드가 없어야 합니다.");

        }

        @Test
        void 실패_400_body_없음() {
            MvcTestResult exchange = mvcTester.put().uri("/post/{postId}", postResponseDto.postId())
                    .header(HttpHeaders.AUTHORIZATION, bearer(access_token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .exchange();

            Assertions.assertThat(exchange).hasStatus(HttpStatus.BAD_REQUEST);
        }

        @Test
        void 실패_403_작성자_외의_수정_요청() {
            LoginResponseDto logined = loginAsAnotherMember();

            MvcTestResult exchange = modifyPostApi(postResponseDto.postId(), PostFixture.getModifyPostRequest(), bearer(logined.token().accessToken()));

            Assertions.assertThat(exchange).hasStatus(HttpStatus.FORBIDDEN);
        }

        private PostResponseDto modifyAndToResponse(Long postId, ModifyPostRequest modifyPostRequest) {
            MvcTestResult exchange = modifyPostApi(postId, modifyPostRequest, bearer(access_token));

            Assertions.assertThat(exchange).hasStatusOk();

            return testFixture.readBody(exchange, PostResponseDto.class);
        }
    }

    @Nested
    @DisplayName("게시글 정지")
    class SuspendPostApi {
        @BeforeEach
        void setup() {
            createAndToResponse(PostFixture.getCreatePostRequest());
        }

        @Test
        void 성공() {
            PostResponseDto suspendedPost = suspendAndToResponse(postResponseDto.postId());

            Assertions.assertThat(suspendedPost.status()).isEqualTo(PostStatus.SUSPENDED);
        }

        private PostResponseDto suspendAndToResponse(Long postId) {
            MvcTestResult exchange = suspendPostApi(postId, bearer(access_token));

            Assertions.assertThat(exchange).hasStatusOk();

            return testFixture.readBody(exchange, PostResponseDto.class);
        }

        @Test
        void 실패_400_잘못된_post_id() {
            MvcTestResult exchange = invalidPathIdApi(HttpMethod.POST, "/post/{id}/suspend");

            testFixture.assertProblem(exchange, HttpStatus.BAD_REQUEST, "Failed to convert 'id' with value: " + "'" + INVALID_PATH_ID + "'");
        }
    }

    @Nested
    @DisplayName("게시글 삭제")
    class DeletePostApi {
        @BeforeEach
        void setup() {
            createAndToResponse(PostFixture.getCreatePostRequest());
        }

        @Test
        void 성공() {
            MvcTestResult exchange = deletePostApi(postResponseDto.postId(), bearer(access_token));

            Assertions.assertThat(exchange).hasStatusOk();

            checkPostNotFound(postResponseDto.postId());
        }

        @Test
        void 실패_403_작성자_외의_삭제_요청() {
            LoginResponseDto anotherLogin = loginAsAnotherMember();

            MvcTestResult result = deletePostApi(postResponseDto.postId(), bearer(anotherLogin.token().accessToken()));

            testFixture.assertProblem(result, HttpStatus.FORBIDDEN, "작성자만 삭제할 수 있습니다.");
        }

        @Test
        void 실패_400_잘못된_post_id() {
            MvcTestResult exchange = invalidPathIdApi(HttpMethod.DELETE, "/post/{id}");

            testFixture.assertProblem(exchange, HttpStatus.BAD_REQUEST, "Failed to convert 'id' with value: " + "'" + INVALID_PATH_ID + "'");
        }

        private void checkPostNotFound(Long postId) {
            MvcTestResult postMvc = findPostApi(postId);

            testFixture.assertProblem(postMvc, HttpStatus.NOT_FOUND, "없는 게시글입니다.");
        }

        private MvcTestResult findPostApi(Long postId) {
            return mvcTester.get().uri("/post/" + postId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .exchange();
        }
    }

    protected MvcTestResult createPostApi(Object request, String token) {
        return mvcTester.post().uri("/post")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(testFixture.toJson(request))
                .exchange();
    }

    protected MvcTestResult modifyPostApi(Long postId, Object modifyPostRequest, String token) {
        return mvcTester.put().uri("/post/" + postId)
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(testFixture.toJson(modifyPostRequest))
                .exchange();
    }

    protected MvcTestResult suspendPostApi(Long postId, String token) {
        return mvcTester.post().uri("/post/" + postId + "/suspend")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange();
    }

    protected @NonNull MvcTestResult deletePostApi(Long postId, String token) {
        return mvcTester.delete().uri("/post/" + postId)
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange();
    }

    protected @NonNull MvcTestResult invalidPathIdApi(HttpMethod method, String urlTemplate) {
        return mvcTester.method(method).uri(urlTemplate, INVALID_PATH_ID)
                .header(HttpHeaders.AUTHORIZATION, bearer(access_token))
                .contentType(MediaType.APPLICATION_JSON)
                .exchange();
    }

    private LoginResponseDto loginAsAnotherMember() {
        Member anotherMember = memberRegister.register(MemberFixture.getRegisterRequest("qwesdf@naver.com"));
        anotherMember.activate();

        return memberLogin.login(anotherMember.getEmail(), MemberFixture.DEFAULT_PASSWORD);
    }

    private PostResponseDto createAndToResponse(CreatePostRequest createPostRequest) {
        MvcTestResult exchange = createPostApi(createPostRequest, bearer(access_token));

        postResponseDto = testFixture.readBody(exchange, PostResponseDto.class);

        Assertions.assertThat(postResponseDto.postId()).isNotNull();

        return postResponseDto;
    }

    private static String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }
}
