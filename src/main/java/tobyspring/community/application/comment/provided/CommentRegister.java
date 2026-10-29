package tobyspring.community.application.comment.provided;

import tobyspring.community.domain.comment.Comment;
import tobyspring.community.domain.comment.CreateCommentRequest;
import tobyspring.community.domain.comment.ModifyCommentRequest;
import tobyspring.community.domain.post.ModifyPostRequest;

public interface CommentRegister {
    Comment create(Long postId, Long memberId, CreateCommentRequest createCommentRequest);

    Comment modify(Long postId, Long memberId, Long commentId, ModifyCommentRequest createCommentRequest);

    Comment postComment(Long postId, Long memberId, Long commentId);

    Comment hide(Long postId, Long memberId, Long commentId);

    Comment suspend(Long postId, Long memberId, Long commentId);
}
