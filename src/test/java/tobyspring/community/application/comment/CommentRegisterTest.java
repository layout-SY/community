package tobyspring.community.application.comment;

import jakarta.transaction.Transactional;
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
import tobyspring.community.application.comment.provided.CommentDetail;
import tobyspring.community.application.comment.provided.CommentFinder;
import tobyspring.community.application.comment.provided.CommentRegister;
import tobyspring.community.application.comment.requried.CommentRepository;
import tobyspring.community.application.member.exception.NotActivatedUserException;
import tobyspring.community.application.member.exception.NotFoundException;
import tobyspring.community.application.member.provided.MemberFinder;
import tobyspring.community.application.member.provided.MemberRegister;
import tobyspring.community.application.post.exception.NotAuthorException;
import tobyspring.community.application.post.provided.PostDetail;
import tobyspring.community.application.post.provided.PostFinder;
import tobyspring.community.application.post.provided.PostRegister;
import tobyspring.community.domain.comment.Comment;
import tobyspring.community.domain.comment.CommentFixture;
import tobyspring.community.domain.comment.CommentStatus;
import tobyspring.community.domain.member.Member;
import tobyspring.community.domain.member.MemberFixture;
import tobyspring.community.domain.post.Post;
import tobyspring.community.domain.post.PostFixture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Transactional
public class CommentRegisterTest {
    private final MemberRegister memberRegister;
    private final PostRegister postRegister;

    private final CommentRegister commentRegister;
    private final CommentFinder commentFinder;
    private final CommentRepository commentRepository;

    private Member member;
    private Member anotherMember;
    private PostDetail post;

    @BeforeEach
    void setUp() {
        member = memberRegister.register(MemberFixture.getRegisterRequest());
        member.activate();

        anotherMember = memberRegister.register(MemberFixture.getRegisterRequest("asdf@naver.com"));
        anotherMember.activate();

        post = postRegister.post(PostFixture.getCreatePostRequest(), member.getId());
    }

    @Nested
    @DisplayName("댓글 생성")
    class CreatePost {
        @Test
        void 성공() {
            CommentDetail detail = commentRegister.create(post.post().getId(), anotherMember.getId(), CommentFixture.createCommentRequest(null));

            assertThat(commentRepository.findById(detail.comment().getId())).isPresent();

            assertThat(detail.authorNickname()).isEqualTo(anotherMember.getNickname());
            assertThat(detail.postTitle()).isEqualTo(post.post().getTitle());
        }

        @Test
        void 실패_비활성_회원() {
            Member inactive = memberRegister.register(MemberFixture.getRegisterRequest("other@test.com"));

            assertThatThrownBy(() -> commentRegister.create(post.post().getId(), inactive.getId(), CommentFixture.createCommentRequest(null)))
                    .isInstanceOf(NotActivatedUserException.class);
        }

        @Test
        void 실패_게시중이_아닌_게시글() {
            postRegister.suspend(post.post().getId(), member.getId());

            assertThatThrownBy(() -> commentRegister.create(post.post().getId(), member.getId(), CommentFixture.createCommentRequest(null)))
                    .isInstanceOf(NotPostedException.class);
        }

        @Test
        void 실패_존재하지_않는_부모_댓글() {
            assertThatThrownBy(
                    ()-> commentRegister.create(post.post().getId(), member.getId(), CommentFixture.createCommentRequest(999L))
            ).isInstanceOf(NotFoundException.class)
                    .hasMessage("id에 해당하는 댓글을 찾을 수 없습니다 : " + 999L);
        }
    }

    @Nested
    @DisplayName("댓글 수정")
    class ModifyComment {
        CommentDetail commentDetail;

        @BeforeEach
        void setup() {
            commentDetail = commentRegister.create(post.post().getId(), anotherMember.getId(), CommentFixture.createCommentRequest(null));

            assertThat(commentDetail.comment().getCreatedAt()).isNotNull();
        }

        @Test
        void 성공() {
            commentRegister.modify(post.post().getId(), anotherMember.getId(), commentDetail.comment().getId(), CommentFixture.modifyCommentRequest());

            CommentDetail commentById = commentFinder.getCommentById(commentDetail.comment().getId());

            assertThat(commentById.comment().getModifiedAt()).isNotNull();
            assertThat(commentById.comment().getMemberId()).isEqualTo(anotherMember.getId());
            assertThat(commentById.comment().getPostId()).isEqualTo(post.post().getId());
            assertThat(commentById.comment().getContent().content()).isEqualTo(CommentFixture.MODIFY_COMMENT_CONTENT);
            assertThat(commentById.comment().getStatus()).isEqualTo(CommentFixture.COMMON_COMMENT_STATUS);
        }

        @Test
        void 성공_posted_외에_상태에서도_수정_가능() {
            commentRegister.modify(post.post().getId(), anotherMember.getId(), commentDetail.comment().getId(), CommentFixture.modifyCommentRequest());

            CommentDetail commentById = commentFinder.getCommentById(commentDetail.comment().getId());

            assertThat(commentById.comment().getStatus()).isEqualTo(CommentFixture.COMMON_COMMENT_STATUS);

            commentRegister.modify(post.post().getId(), anotherMember.getId(), commentDetail.comment().getId(), CommentFixture.modifyCommentRequest("상태 수정", CommentStatus.POSTED));

            commentRegister.modify(post.post().getId(), anotherMember.getId(), commentDetail.comment().getId(), CommentFixture.modifyCommentRequest("상태 수정", CommentStatus.SUSPENDED));
        }

        @Test
        void 실패_작성자_외의_수정() {
            assertThatThrownBy(
                    () -> commentRegister.modify(post.post().getId(), member.getId(), commentDetail.comment().getId(), CommentFixture.modifyCommentRequest())
            ).isInstanceOf(NotAuthorException.class)
                    .hasMessage("작성자만 수정할 수 있습니다.");
        }

        @Test
        void 실패_게시글에_없는_댓글() {
            Post post2 = postRegister.post(PostFixture.getCreatePostRequest(), anotherMember.getId()).post();

            assertThatThrownBy(
                    () -> commentRegister.modify(post2.getId(), member.getId(), commentDetail.comment().getId(), CommentFixture.modifyCommentRequest())
            ).isInstanceOf(NotFoundException.class)
                    .hasMessage("게시글에 해당하는 댓글을 찾을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("게시글 숨김/정지/게시")
    class ChangeStatus {
        CommentDetail commentDetail;

        @BeforeEach
        void setup() {
            commentDetail = commentRegister.create(post.post().getId(), member.getId(), CommentFixture.createCommentRequest(null));
        }

        @Test
        void 성공_숨김() {
            commentRegister.hide(post.post().getId(), member.getId(), commentDetail.comment().getId());

            assertThat(commentDetail.comment().getStatus()).isEqualTo(CommentStatus.HIDDEN);
        }

        @Test
        void 성공_정지() {
            commentRegister.suspend(post.post().getId(), member.getId(), commentDetail.comment().getId());

            commentDetail.comment().suspend();

            assertThat(commentDetail.comment().getStatus()).isEqualTo(CommentStatus.SUSPENDED);
        }

        @Test
        void 성공_게시() {
            commentDetail.comment().hide();

            assertThat(commentDetail.comment().getStatus()).isEqualTo(CommentStatus.HIDDEN);

            commentDetail.comment().postComment();

            assertThat(commentDetail.comment().getStatus()).isEqualTo(CommentStatus.POSTED);
        }

        @Test
        void 실패_작성자_외의_변경() {
            assertThatThrownBy(
                    () -> commentRegister.hide(post.post().getId(), anotherMember.getId(), commentDetail.comment().getId())
            ).isInstanceOf(NotAuthorException.class)
                    .hasMessage("작성자만 수정할 수 있습니다.");

            assertThatThrownBy(
                    () -> commentRegister.suspend(post.post().getId(), anotherMember.getId(), commentDetail.comment().getId())
            ).isInstanceOf(NotAuthorException.class)
                    .hasMessage("작성자만 수정할 수 있습니다.");

            assertThatThrownBy(
                    () -> commentRegister.postComment(post.post().getId(), anotherMember.getId(), commentDetail.comment().getId())
            ).isInstanceOf(NotAuthorException.class)
                    .hasMessage("작성자만 수정할 수 있습니다.");
        }


    }
}
