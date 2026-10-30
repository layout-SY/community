package tobyspring.community.application.comment.provided;

import tobyspring.community.domain.comment.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentFinder {
    CommentDetail getCommentById(Long commentId);

    List<CommentDetail> getComments();

    List<CommentDetail> getChildCommentsById(Long commentId);
}
