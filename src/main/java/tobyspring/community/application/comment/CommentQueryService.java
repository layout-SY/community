package tobyspring.community.application.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import tobyspring.community.application.comment.provided.CommentDetail;
import tobyspring.community.application.comment.provided.CommentFinder;
import tobyspring.community.application.comment.requried.CommentRepository;
import tobyspring.community.application.member.exception.NotFoundException;
import tobyspring.community.application.member.provided.MemberFinder;
import tobyspring.community.application.post.provided.PostDetail;
import tobyspring.community.application.post.provided.PostFinder;
import tobyspring.community.domain.comment.Comment;
import tobyspring.community.domain.member.Member;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentQueryService implements CommentFinder {
    private final CommentRepository commentRepository;
    private final MemberFinder memberFinder;
    private final PostFinder postFinder;

    @Override
    public CommentDetail getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new NotFoundException("id에 해당하는 댓글을 찾을 수 없습니다 : " + commentId)
        );

        PostDetail postDetailById = postFinder.findById(comment.getPostId());

        Member memberById = memberFinder.findById(comment.getMemberId());

        return CommentDetail.mapper(comment, memberById.getNickname(), postDetailById.post().getTitle());
    }

    @Override
    public List<CommentDetail> getComments() {
        return List.of();
    }

    @Override
    public List<CommentDetail> getChildCommentsById(Long commentId) {
        return List.of();
    }
}
