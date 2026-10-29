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

            Assertions.assertThat(parentComment.getContent().content()).isEqualTo(CommentFixture.COMMENT_CONTENT);
            Assertions.assertThat(parentComment.getPostId()).isEqualTo(POST_ID);
            Assertions.assertThat(parentComment.getMemberId()).isEqualTo(MEMBER_ID);
            Assertions.assertThat(parentComment.getStatus()).isEqualTo(CommentStatus.POSTED);
            Assertions.assertThat(parentComment.getCreatedAt()).isNotNull();
            Assertions.assertThat(parentComment.getModifiedAt()).isNull();
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
                    .hasMessage("댓글이(가) 없습니다.");
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

    @Nested
    @DisplayName("댓글 수정")
    class ModifyComment {
        Comment comment;

        @BeforeEach
        void setUp() {
            comment = Comment.create(POST_ID, MEMBER_ID, CommentFixture.createCommentRequest(null));
        }

        @Test
        void 성공() {
            comment.modify(CommentFixture.modifyCommentRequest());

            Assertions.assertThat(comment.getPostId()).isEqualTo(POST_ID);
            Assertions.assertThat(comment.getModifiedAt()).isNotNull();
            Assertions.assertThat(comment.getContent().content()).isEqualTo(CommentFixture.MODIFY_COMMENT_CONTENT);
            Assertions.assertThat(comment.getStatus()).isEqualTo(CommentStatus.HIDDEN);
        }

        @Test
        void 성공_null_필드는_기존_유지_content() {
            String previousContent = comment.getContent().content();

            comment.modify(CommentFixture.modifyCommentRequest(null, CommentStatus.SUSPENDED));

            Assertions.assertThat(comment.getContent().content()).isEqualTo(previousContent);
            Assertions.assertThat(comment.getStatus()).isEqualTo(CommentStatus.SUSPENDED);
        }

        @Test
        void 성공_null_필드는_기존_유지_status() {
            CommentStatus previousStatus = comment.getStatus();

            comment.modify(CommentFixture.modifyCommentRequest("수정됐어요", null));

            Assertions.assertThat(comment.getStatus()).isEqualTo(previousStatus);
            Assertions.assertThat(comment.getContent().content()).isEqualTo("수정됐어요");
        }
    }
}
