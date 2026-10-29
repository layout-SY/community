package tobyspring.community.application.comment.provided;

import tobyspring.community.domain.comment.Comment;
import tobyspring.community.domain.comment.CreateCommentRequest;
import tobyspring.community.domain.comment.ModifyCommentRequest;
import tobyspring.community.domain.post.ModifyPostRequest;

public interface CommentRegister {
    CommentDetail create(Long postId, Long memberId, CreateCommentRequest createCommentRequest);

    CommentDetail modify(Long postId, Long memberId, Long commentId, ModifyCommentRequest createCommentRequest);

    CommentDetail postComment(Long postId, Long memberId, Long commentId);

    CommentDetail hide(Long postId, Long memberId, Long commentId);

    CommentDetail suspend(Long postId, Long memberId, Long commentId);
}
