package tobyspring.community.application.comment.provided;

import tobyspring.community.domain.comment.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentFinder {
    Comment getCommentById(Long commentId);

    List<Comment> getComments();

    List<Comment> getChildCommentsById(Long commentId);
}
