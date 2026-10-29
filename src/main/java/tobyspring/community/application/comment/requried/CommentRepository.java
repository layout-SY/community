package tobyspring.community.application.comment.requried;

import org.springframework.data.repository.Repository;
import tobyspring.community.domain.comment.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends Repository<Comment, Long> {
    Comment save(Comment comment);

    Optional<Comment> findById(Long commentId);

    Optional<List<Comment>> findComments();

    Optional<List<Comment>> findCommentsById(Long commentId);
}
