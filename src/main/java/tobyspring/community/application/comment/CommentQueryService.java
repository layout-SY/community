package tobyspring.community.application.comment;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentQueryService implements CommentFinder {
    private final CommentRepository commentRepository;
    private final MemberFinder memberFinder;
    private final PostFinder postFinder;

    private static final String UNKNOWN_AUTHOR = "알 수 없는 작성자";
    private static final String UNKNOWN_POST_TITLE = "알 수 없는 제목";

    @Override
    public CommentDetail getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new NotFoundException("id에 해당하는 댓글을 찾을 수 없습니다 : " + commentId)
        );

        return toDetails(List.of(comment)).get(0);
    }

    @Override
    public List<CommentDetail> getComments() {
        List<Comment> all = commentRepository.findAll();

        return toDetails(all);
    }

    @Override
    public List<CommentDetail> getChildCommentsById(Long commentId) {
        List<Comment> allByParentCommentId = commentRepository.findAllByParentCommentId(commentId);

        return toDetails(allByParentCommentId);
    }

    @Override
    public List<CommentDetail> getChildCommentsByPostId(Long postId) {
        PostDetail postDetailById = getPostDetail(postId);

        List<Comment> childCommentsByPostId = commentRepository.findAllByPostIdAndParentCommentIdIsNotNull(postDetailById.post().getId());

        return toDetails(childCommentsByPostId);
    }

    @Override
    public List<CommentDetail> getCommentsByPostId(Long postId) {
        PostDetail postDetailById = getPostDetail(postId);

        List<Comment> allByPostId = commentRepository.findAllByPostId(postDetailById.post().getId());

        return toDetails(allByPostId);
    }

    private List<CommentDetail> toDetails(List<Comment> comments) {
        Map<Long, String> nicknamesByMemberId = memberFinder.getNicknameByIds(
                comments.stream().map(Comment::getMemberId).collect(Collectors.toSet())
        );
        Map<Long, String> titlesByPostId = postFinder.getTitleByPostIds(
                comments.stream().map(Comment::getPostId).collect(Collectors.toSet())
        );

        return comments.stream()
                .map(comment -> CommentDetail.mapper(
                        comment,
                        nicknamesByMemberId.getOrDefault(comment.getMemberId(), UNKNOWN_AUTHOR),
                        titlesByPostId.getOrDefault(comment.getPostId(), UNKNOWN_POST_TITLE)
                ))
                .toList();
    }

    private PostDetail getPostDetail(Long postId) {
        return postFinder.findById(postId);
    }
}