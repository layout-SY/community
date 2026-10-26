package tobyspring.community.adapter.webapi;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
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
import tobyspring.community.domain.post.CreatePostRequest;
import tobyspring.community.domain.post.Post;
import tobyspring.community.domain.post.PostCategory;
import tobyspring.community.domain.post.PostFixture;

import java.util.ArrayList;

@SpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@AutoConfigureMockMvc
@Transactional
@Import(TestFixture.class)
public class PostControllerTest {
    private final MockMvcTester mvcTester;
    private final PostRepository postRepository;
    private final TokenManager tokenManager;
    private final TestFixture testFixture;
    private final MemberRegister memberRegister;
    private final MemberLogin memberLogin;

    private String BEARER_PREFIX = "Bearer ";

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

            String requestBody = testFixture.toJson(createPostRequest);

            MvcTestResult exchange = mvcTester.post().uri("/post")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + loginResponseDto.token().accessToken())
                    .content(requestBody)
                    .exchange();

            PostResponseDto postResponseDto = testFixture.readBody(exchange, PostResponseDto.class);

            Assertions.assertThat(postResponseDto.createdAt()).isNotNull();
            Assertions.assertThat(postResponseDto.postId()).isNotNull();
            Assertions.assertThat(postResponseDto.memberNickname()).isEqualTo(member.getNickname());
        }

        @Test
        void 실패_401_유효한_토큰_아님() {
            CreatePostRequest createPostRequest = PostFixture.getCreatePostRequest();

            String requestBody = testFixture.toJson(createPostRequest);

            MvcTestResult exchange = mvcTester.post().uri("/post")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + "invalid-token")
                    .content(requestBody)
                    .exchange();

            ProblemDetail problemDetail = testFixture.readBody(exchange, ProblemDetail.class);

            Assertions.assertThat(exchange).hasStatus(HttpStatus.UNAUTHORIZED);
            Assertions.assertThat(problemDetail.getDetail()).isEqualTo("유효하지 않은 토큰입니다.");
        }

        @Test
        void 실패_401_토큰_헤더_없음() {
            CreatePostRequest createPostRequest = PostFixture.getCreatePostRequest();

            String requestBody = testFixture.toJson(createPostRequest);

            MvcTestResult exchange = mvcTester.post().uri("/post")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .exchange();

            ProblemDetail problemDetail = testFixture.readBody(exchange, ProblemDetail.class);

            Assertions.assertThat(exchange).hasStatus(HttpStatus.BAD_REQUEST);
            Assertions.assertThat(problemDetail.getDetail()).isEqualTo("Required header 'Authorization' is not present.");
        }

        @Test
        void 실패_401_유효하지_않은_헤더() {
            CreatePostRequest createPostRequest = PostFixture.getCreatePostRequest();

            String requestBody = testFixture.toJson(createPostRequest);

            MvcTestResult exchange = mvcTester.post().uri("/post")
                    .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .exchange();

            ProblemDetail problemDetail = testFixture.readBody(exchange, ProblemDetail.class);

            Assertions.assertThat(exchange).hasStatus(HttpStatus.UNAUTHORIZED);
            Assertions.assertThat(problemDetail.getDetail()).isEqualTo("Authorization 헤더가 올바르지 않습니다");
        }

        @Test
        void 성공_400_요청_body_선택_파라미터_누락() {
            ArrayList<String> list = new ArrayList<>();
            list.add("java");

            CreatePostRequest createPostRequestCategoryNull = new CreatePostRequest("title", "contentcontent", null, list, list);
            CreatePostRequest createPostRequestImageNull = new CreatePostRequest("titletitle", "contentcontent", PostCategory.IT, list, null);
            CreatePostRequest createPostRequestTagsNull = new CreatePostRequest("title", "contentcontent", PostCategory.IT, null, list);

            commonCreatePostSuccess("category", createPostRequestCategoryNull);
            commonCreatePostSuccess("tags", createPostRequestTagsNull);
            commonCreatePostSuccess("imageUrls", createPostRequestImageNull);
        }

        @Test
        void 실패_400_요청_body_필수_파라미터_누락() {
            ArrayList<String> list = new ArrayList<>();
            list.add("java");

            CreatePostRequest createPostRequest2 = new CreatePostRequest("titletitle", null, PostCategory.IT, list, null);
            CreatePostRequest createPostRequest4 = new CreatePostRequest(null, "contentcontent", null, list, list);

            createPostRequestTest("title", createPostRequest4);
            createPostRequestTest("content", createPostRequest2);
        }

        private void createPostRequestTest(String sign, CreatePostRequest createPostRequest) {
            ProblemDetail problemDetail = commonCreatePostFail(createPostRequest);

            Assertions.assertThat(problemDetail.getDetail()).isEqualTo(sign + "은 필수 입력입니다. : " + "null");
        }

        private void commonCreatePostSuccess(String sign, CreatePostRequest createPostRequest) {
            String requestBody = testFixture.toJson(createPostRequest);

            MvcTestResult exchange = mvcTester.post().uri("/post")
                    .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + loginResponseDto.token().accessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .exchange();

            Assertions.assertThat(exchange).hasStatusOk();
            PostResponseDto postResponseDto = testFixture.readBody(exchange, PostResponseDto.class);

            if(sign.equals("category")) Assertions.assertThat(postResponseDto.category()).isEqualTo(PostCategory.ETC);
            if(sign.equals("tags")) Assertions.assertThat(postResponseDto.tags()).isEmpty();
            if(sign.equals("imageUrls")) Assertions.assertThat(postResponseDto.imageUrls()).isEmpty();
        }

        private ProblemDetail commonCreatePostFail(CreatePostRequest createPostRequest) {
            String requestBody = testFixture.toJson(createPostRequest);

            MvcTestResult exchange = mvcTester.post().uri("/post")
                    .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + loginResponseDto.token().accessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .exchange();

            Assertions.assertThat(exchange).hasStatus(HttpStatus.BAD_REQUEST);

            return testFixture.readBody(exchange, ProblemDetail.class);
        }
    }
}
