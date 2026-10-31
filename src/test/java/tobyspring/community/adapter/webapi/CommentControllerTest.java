package tobyspring.community.adapter.webapi;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
import tobyspring.community.adapter.webapi.comment.CommentResponseDto;
import tobyspring.community.adapter.webapi.post.PostResponseDto;
import tobyspring.community.application.member.provided.MemberLogin;
import tobyspring.community.application.member.provided.MemberRegister;
import tobyspring.community.application.post.provided.PostDetail;
import tobyspring.community.application.post.provided.PostRegister;
import tobyspring.community.domain.comment.*;
import tobyspring.community.domain.member.LoginResponseDto;
import tobyspring.community.domain.member.Member;
import tobyspring.community.domain.member.MemberFixture;
import tobyspring.community.domain.post.*;

import java.util.stream.Stream;

@SpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Transactional
@Import(TestFixture.class)
@AutoConfigureMockMvc
public class CommentControllerTest {
    private final MockMvcTester mvcTester;
    private final TestFixture testFixture;

    private final MemberRegister memberRegister;
    private final MemberLogin memberLogin;

    private final PostRegister postRegister;

    private Member member;
    private String access_token;
    private PostDetail post;

    @BeforeEach
    public void setUp() {
         LoginResponseDto loginResponseDto = registerMemberAndLogin("qwer@naver.com");

        Assertions.assertThat(loginResponseDto.token().accessToken()).isNotNull();

        access_token = loginResponseDto.token().accessToken();

        post = postRegister.post(PostFixture.getCreatePostRequest(), member.getId());

        Assertions.assertThat(post.post().getCreatedAt()).isNotNull();
        Assertions.assertThat(post.post().getStatus()).isEqualTo(PostStatus.POSTED);
    }

    @Nested
    @DisplayName("댓글 생성")
    class CommentCreate {
        @Test
        void 성공() {
            CreateCommentRequest commentRequest = CommentFixture.createCommentRequest(null);

            CommentResponseDto commentToResponse = createToResponse(post.post().getId(), commentRequest);

            Assertions.assertThat(commentToResponse.createdAt()).isNotNull();
            Assertions.assertThat(commentToResponse.id()).isNotNull();
            Assertions.assertThat(commentToResponse.authorNickname()).isEqualTo(member.getNickname());
            Assertions.assertThat(commentToResponse.postTitle()).isEqualTo(post.post().getTitle());
        }

        @ParameterizedTest(name = "{0} 누락")
        @MethodSource("missingRequiredFields")
        void 실패_400_필수_파라미터_누락(String field, CreateCommentRequest request) {
            testFixture.assertProblem(createCommentApi(post.post().getId(), request, bearer(access_token)),
                    HttpStatus.BAD_REQUEST, "댓글이(가) 없습니다.");
        }

        static Stream<Arguments> missingRequiredFields() {
            return Stream.of(
                    Arguments.of("content", new CreateCommentRequest(null, null))
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
            String requestBody = testFixture.toJson(CommentFixture.createCommentRequest(null));

            return mvcTester.post().uri("/comment/create/{postId}", post.post().getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .exchange();
        }

        @Test
        void 실패_401_유효하지_않은_헤더() {
            testFixture.assertProblem(tokenValidator(bearer("")), HttpStatus.UNAUTHORIZED, "Authorization 헤더가 올바르지 않습니다");
        }

        private @NonNull MvcTestResult tokenValidator(String token) {
            CreateCommentRequest createPostRequest = CommentFixture.createCommentRequest(null);

            return createCommentApi(post.post().getId(), createPostRequest, token);
        }
    }

    @Nested
    @DisplayName("댓글 수정")
    class ModifyComment {
        CommentResponseDto commentToResponse;

        @BeforeEach
        void setUp() {
            commentToResponse = createToResponse(post.post().getId(), CommentFixture.createCommentRequest(null));
        }


        @Test
        void 성공() {
            modifyAndToResponse(CommentFixture.modifyCommentRequest(), bearer(access_token));

            Assertions.assertThat(commentToResponse.modifiedAt()).isNotNull();
            Assertions.assertThat(commentToResponse.content()).isEqualTo(CommentFixture.MODIFY_COMMENT_CONTENT);
        }

        private void modifyAndToResponse(ModifyCommentRequest request, String token) {
            MvcTestResult result = modifyCommentApi(post.post().getId(), commentToResponse.id(), request, token);

            Assertions.assertThat(result).hasStatusOk();

            commentToResponse = testFixture.readBody(result, CommentResponseDto.class);
        }

        @Test
        void 실패_403_작성자_외의_수정() {
            LoginResponseDto loginResponseDto = registerMemberAndLogin("zxcv@naver.com");

            String anotherMemberLoginToken = loginResponseDto.token().accessToken();

            MvcTestResult result = modifyCommentApi(post.post().getId(), commentToResponse.id(), CommentFixture.modifyCommentRequest(), bearer(anotherMemberLoginToken));

            testFixture.assertProblem(result, HttpStatus.FORBIDDEN, "작성자가 아닙니다.");
        }

        @Test
        void 실패_400_요청_body_없음() {
            MvcTestResult result = modifyCommentApiNonBody(post.post().getId(), commentToResponse.id(), bearer(access_token));

            testFixture.assertProblem(result, HttpStatus.BAD_REQUEST, "누락된 필드가 없어야 합니다.");
        }

        private MvcTestResult modifyCommentApiNonBody(Long postId, Long commentId, String token) {
            return mvcTester.put().uri("/comment/modify/{postId}/{commentId}", postId, commentId)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .exchange();
        }


        @Test
        void 실패_댓글의_postId_미일치() {
            PostDetail anotherPost = postRegister.post(PostFixture.getCreatePostRequest(), member.getId());

            MvcTestResult result = modifyCommentApi(anotherPost.post().getId(), commentToResponse.id(), CommentFixture.modifyCommentRequest(), bearer(access_token));

            testFixture.assertProblem(result, HttpStatus.NOT_FOUND, "게시글에 해당하는 댓글을 찾을 수 없습니다.");
        }

        @Test
        void 실패_400_정지된_댓글의_수정() {
            changeCommentStatusApi(post.post().getId(), commentToResponse.id(), CommentStatus.SUSPENDED, bearer(access_token));

            MvcTestResult result = modifyCommentApi(post.post().getId(), commentToResponse.id(), CommentFixture.modifyCommentRequest(), bearer(access_token));

            testFixture.assertProblem(result, HttpStatus.BAD_REQUEST, "정지되어 수정할 수 없습니다.");
        }

        @Test
        void 실패_401_유효한_토큰_아님() {
            testFixture.assertProblem(tokenValidator(bearer("invalid-token")), HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
        }

        @Test
        void 실패_400_토큰_헤더_없음() {
            testFixture.assertProblem(nonAuthenticationHeader(post.post().getId(), commentToResponse.id()), HttpStatus.BAD_REQUEST, "Required header 'Authorization' is not present.");
        }

        private MvcTestResult nonAuthenticationHeader(Long postId, Long commentId) {
            String requestBody = testFixture.toJson(PostFixture.getCreatePostRequest());

            return mvcTester.put().uri("/comment/modify/{postId}/{commentId}", postId, commentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .exchange();
        }

        @Test
        void 실패_401_유효하지_않은_헤더() {
            testFixture.assertProblem(tokenValidator(bearer("")), HttpStatus.UNAUTHORIZED, "Authorization 헤더가 올바르지 않습니다");
        }

        private @NonNull MvcTestResult tokenValidator(String token) {
            ModifyCommentRequest request = CommentFixture.modifyCommentRequest();

            return modifyCommentApi(post.post().getId(), commentToResponse.id(), request, token);
        }
    }

    @Nested
    @DisplayName("댓글 상태 변경")
    class ChangeCommentStatus {
        CommentResponseDto commentToResponse;

        @BeforeEach
        void setUp() {
            commentToResponse = createToResponse(post.post().getId(), CommentFixture.createCommentRequest(null));
        }

        @Test
        void 성공() {
            CommentResponseDto changedCommentHidden = changeStatusAndToResponse(post.post().getId(), commentToResponse.id(), CommentStatus.HIDDEN, bearer(access_token));

            Assertions.assertThat(changedCommentHidden.status()).isEqualTo(CommentStatus.HIDDEN);

            CommentResponseDto changedStatusSuspended = changeStatusAndToResponse(post.post().getId(), commentToResponse.id(), CommentStatus.SUSPENDED, bearer(access_token));

            Assertions.assertThat(changedStatusSuspended.status()).isEqualTo(CommentStatus.SUSPENDED);

            CommentResponseDto changedStatusPosted = changeStatusAndToResponse(post.post().getId(), commentToResponse.id(), CommentStatus.POSTED, bearer(access_token));

            Assertions.assertThat(changedStatusPosted.status()).isEqualTo(CommentStatus.POSTED);
        }

        @Test
        void 실패_403_작성자_외의_상태_변경() {
            LoginResponseDto loginResponseDto = registerMemberAndLogin("zxcv@naver.com");

            String anotherMemberLoginToken = loginResponseDto.token().accessToken();

            MvcTestResult result = changeCommentStatusApi(post.post().getId(), commentToResponse.id(), CommentStatus.HIDDEN, bearer(anotherMemberLoginToken));

            testFixture.assertProblem(result, HttpStatus.FORBIDDEN, "작성자가 아닙니다.");
        }

        @Test
        void 실패_404_없는_댓글() {
            MvcTestResult result = changeCommentStatusApi(post.post().getId(), 100L, CommentStatus.HIDDEN, bearer(access_token));

            testFixture.assertProblem(result, HttpStatus.NOT_FOUND, "id에 해당하는 댓글을 찾을 수 없습니다 : " + 100);
        }

        @Test
        void 실패_400_잘못된_status() {
            MvcTestResult result = changeCommentStatusApi(post.post().getId(), commentToResponse.id(), "INVALID_STATUS", bearer(access_token));

            testFixture.assertProblem(result, HttpStatus.BAD_REQUEST, "Failed to convert 'status' with value: " + "'INVALID_STATUS'");
        }

        @Test
        void 실패_같은_status_재요청_POSTED() {
            MvcTestResult result = changeCommentStatusApi(post.post().getId(), commentToResponse.id(), CommentStatus.POSTED, bearer(access_token));

            testFixture.assertProblem(result, HttpStatus.BAD_REQUEST, "이미 게시 중인 댓글입니다.");
        }

        @Test
        void 실패_같은_status_재요청_HIDDEN() {
            changeCommentStatusApi(post.post().getId(), commentToResponse.id(), CommentStatus.HIDDEN, bearer(access_token));

            MvcTestResult result = changeCommentStatusApi(post.post().getId(), commentToResponse.id(), CommentStatus.HIDDEN, bearer(access_token));

            testFixture.assertProblem(result, HttpStatus.BAD_REQUEST, "이미 숨긴 댓글입니다.");
        }

        @Test
        void 실패_같은_status_재요청_SUSPENDED() {
            changeCommentStatusApi(post.post().getId(), commentToResponse.id(), CommentStatus.SUSPENDED, bearer(access_token));

            MvcTestResult result = changeCommentStatusApi(post.post().getId(), commentToResponse.id(), CommentStatus.SUSPENDED, bearer(access_token));

            testFixture.assertProblem(result, HttpStatus.BAD_REQUEST, "이미 정지된 댓글입니다.");
        }

        private CommentResponseDto changeStatusAndToResponse(Long postId, Long commentId, CommentStatus status, String token) {
            MvcTestResult result = changeCommentStatusApi(postId, commentId, status, token);

            Assertions.assertThat(result).hasStatusOk();

            return testFixture.readBody(result, CommentResponseDto.class);
        }
    }

    @Nested
    @DisplayName("댓글 삭제")
    class DeleteComment {
        CommentResponseDto commentToResponse;

        @BeforeEach
        void setUp() {
            commentToResponse = createToResponse(post.post().getId(), CommentFixture.createCommentRequest(null));
        }

        @Test
        void 성공() {
            Long commentId = commentToResponse.id();

            deleteCommentToResponse(commentId, bearer(access_token));

            MvcTestResult result = getCommentById(commentId);

            testFixture.assertProblem(result, HttpStatus.NOT_FOUND, "id에 해당하는 댓글을 찾을 수 없습니다 : " + commentId);
        }

        private void deleteCommentToResponse(Long commentId, String token) {
            MvcTestResult result = deleteCommentApi(commentId, token);

            Assertions.assertThat(result).hasStatusOk();
        }

        @Test
        void 실패_작성자_외의_수정() {
            LoginResponseDto loginResponseDto = registerMemberAndLogin("zxcv@naver.com");

            String anotherMemberLoginToken = loginResponseDto.token().accessToken();

            MvcTestResult result = deleteCommentApi(commentToResponse.id(), bearer(anotherMemberLoginToken));

            testFixture.assertProblem(result, HttpStatus.FORBIDDEN, "작성자가 아닙니다.");
        }
    }

    private MvcTestResult createCommentApi(Long postId, Object request, String token) {
        return mvcTester.post().uri("/comment/create/{postId}", postId)
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(testFixture.toJson(request))
                .exchange();
    }

    private MvcTestResult modifyCommentApi(Long postId, Long commentId, Object request, String token) {
        return mvcTester.put().uri("/comment/modify/{postId}/{commentId}", postId, commentId)
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(testFixture.toJson(request))
                .exchange();
    }

    private MvcTestResult changeCommentStatusApi(Long postId, Long commentId, Object status, String token) {
        return mvcTester.put().uri("/comment/change-status/{postId}/{commentId}/{status}", postId, commentId, status)
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange();
    }

    private MvcTestResult deleteCommentApi(Long commentId, String token) {
        return mvcTester.delete().uri("/comment/delete/{commentId}", commentId)
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange();
    }

    private MvcTestResult getCommentById(Long commentId) {
        return mvcTester.get().uri("/comment/{commentId}", commentId)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange();
    }

    private CommentResponseDto createToResponse(Long postId, CreateCommentRequest commentRequest) {
        MvcTestResult result = createCommentApi(postId, commentRequest, bearer(access_token));

        CommentResponseDto commentResponseDto = testFixture.readBody(result, CommentResponseDto.class);

        Assertions.assertThat(commentResponseDto.content()).isNotNull();

        return commentResponseDto;
    }

    private LoginResponseDto registerMemberAndLogin(String email) {
        member = memberRegister.register(MemberFixture.getRegisterRequest(email));
        member.activate();

        return memberLogin.login(member.getEmail(), MemberFixture.DEFAULT_PASSWORD);
    }

    private static String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }
}
