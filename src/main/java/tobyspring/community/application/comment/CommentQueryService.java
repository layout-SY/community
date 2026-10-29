package tobyspring.community.application.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import tobyspring.community.application.comment.provided.CommentFinder;
import tobyspring.community.application.comment.requried.CommentRepository;
import tobyspring.community.application.member.exception.NotFoundException;
import tobyspring.community.domain.comment.Comment;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentQueryService implements CommentFinder {
    private final CommentRepository commentRepository;

    @Override
    public Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(
                () -> new NotFoundException("id에 해당하는 댓글을 찾을 수 없습니다 : " + commentId)
        );
    }

    @Override
    public List<Comment> getComments() {
        return List.of();
    }

    @Override
    public List<Comment> getChildCommentsById(Long commentId) {
        return List.of();
    }
}
