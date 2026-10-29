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
import tobyspring.community.application.post.provided.PostDetail;
import tobyspring.community.application.post.provided.PostFinder;
import tobyspring.community.application.post.provided.PostRegister;
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
    private PostDetail post;

    @BeforeEach
    void setUp() {
        member = memberRegister.register(MemberFixture.getRegisterRequest());
        member.activate();

        post = postRegister.post(PostFixture.getCreatePostRequest(), member.getId());
    }

    @Nested
    @DisplayName("댓글 생성")
    class CreatePost {
        @Test
        void 성공() {
            CommentDetail detail = commentRegister.create(post.post().getId(), member.getId(), CommentFixture.createCommentRequest(null));

            assertThat(commentRepository.findById(detail.comment().getId())).isPresent();

            assertThat(detail.authorNickname()).isEqualTo(member.getNickname());
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
}
