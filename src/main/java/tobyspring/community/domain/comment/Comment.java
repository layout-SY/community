package tobyspring.community.domain.comment;

import io.jsonwebtoken.lang.Assert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tobyspring.community.domain.EntityAbstract;
import tobyspring.community.domain.post.PostStatus;

import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

@Entity
@Table
@NoArgsConstructor
@ToString
@Getter
public class Comment extends EntityAbstract {

    private String content;

    private Long postId;

    private Long memberId;

    private Long likes = 0L;

    private CommentStatus status;

    private SubComments subComments;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    public static Comment register(Long postId, Long memberId, CreateCommentRequest createCommentRequest) {
        Comment comment = new Comment();

        requireNonNull(createCommentRequest, "요청 body가 null입니다.");

        comment.content = requireNonNull(createCommentRequest.content(), "댓글이 없습니다.");
        comment.postId = requireNonNull(postId, "postId가 null입니다.");
        comment.memberId = requireNonNull(memberId, "memberId가 null입니다.");

        comment.status = CommentStatus.POSTED;
        comment.createdAt = LocalDateTime.now();

        return comment;
    }

    public void modify(ModifyCommentRequest modifyCommentRequest) {
        requireNonNull(modifyCommentRequest, "요청 body가 null입니다.");

        content = modifyCommentRequest.contentValue().orElse(this.content);
        status = modifyCommentRequest.statusValue().orElse(this.status);

        modifiedAt = LocalDateTime.now();
    }

    public void like() {
        Assert.state(status == CommentStatus.POSTED, "게시 중인 댓글만 좋아요할 수 있습니다.");
        likes = this.likes + 1;
    }

    public void unlike() {
        Assert.state(status == CommentStatus.POSTED, "게시 중인 댓글만 좋아요할 수 있습니다.");
        likes = this.likes - 1;
    }

    public void hide() {
        Assert.state(status == CommentStatus.POSTED, "게시 중인 댓글만 숨길 수 있습니다.");
    }
}
