package tobyspring.community.application.comment.requried;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import tobyspring.community.domain.comment.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends Repository<Comment, Long> {
    void save(Comment comment);

    Optional<Comment> findById(Long commentId);

    List<Comment> findAll();

    List<Comment> findAllByPostIdAndParentCommentIdIsNotNull(Long postId);

    List<Comment> findAllByPostId(Long postId);

    List<Comment> findAllByParentCommentId(Long parentCommentId);

    void deleteById(Long commentId);
}
