package tobyspring.community.domain.comment;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import tobyspring.community.TestFixture;
import tobyspring.community.application.member.provided.MemberRegister;
import tobyspring.community.domain.member.Member;
import tobyspring.community.domain.member.MemberFixture;

@SpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Import(TestFixture.class)
public class CommentTest {
    private final Long MEMBER_ID = 1L;
    private final Long POST_ID = 10L;

    @Nested
    @DisplayName("부모 댓글 생성")
    class CreateComment {
        @Test
        void 성공_일반_댓글() {
            Comment parentComment = Comment.create(POST_ID, MEMBER_ID, CommentFixture.createCommentRequest(null));

            Assertions.assertThat(parentComment.getContent()).isEqualTo(CommentFixture.COMMENT_CONTENT);
            Assertions.assertThat(parentComment.getPostId()).isEqualTo(POST_ID);
            Assertions.assertThat(parentComment.getMemberId()).isEqualTo(MEMBER_ID);
            Assertions.assertThat(parentComment.getStatus()).isEqualTo(CommentStatus.POSTED);
            Assertions.assertThat(parentComment.getCreatedAt()).isNotNull();
        }

        @Test
        void 실패_요청_body_null() {
            Assertions.assertThatThrownBy(() -> Comment.create(POST_ID, MEMBER_ID, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("요청 body가 null입니다.");
        }

        @Test
        void 실패_요청_content_null() {
            Assertions.assertThatThrownBy(() -> Comment.create(POST_ID, MEMBER_ID, CommentFixture.createCommentRequest(null, null)))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("댓글이 없습니다.");
        }

        @Test
        void 실패_요청_postId_null() {
            Assertions.assertThatThrownBy(() -> Comment.create(null, MEMBER_ID, CommentFixture.createCommentRequest(null)))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("postId는 필수입니다.");
        }

        @Test
        void 실패_요청_memberId_null() {
            Assertions.assertThatThrownBy(() -> Comment.create(POST_ID, null, CommentFixture.createCommentRequest(null)))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("memberId는 필수입니다.");
        }
    }
}
