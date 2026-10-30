package tobyspring.community.application.comment;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import tobyspring.community.application.comment.provided.CommentDetail;
import tobyspring.community.application.comment.provided.CommentFinder;
import tobyspring.community.application.comment.provided.CommentRegister;
import tobyspring.community.application.comment.requried.CommentRepository;
import tobyspring.community.application.member.exception.NotActivatedUserException;
import tobyspring.community.application.member.exception.NotFoundException;
import tobyspring.community.application.member.provided.MemberFinder;
import tobyspring.community.application.post.exception.NotAuthorException;
import tobyspring.community.application.post.provided.PostDetail;
import tobyspring.community.application.post.provided.PostFinder;
import tobyspring.community.domain.comment.Comment;
import tobyspring.community.domain.comment.CreateCommentRequest;
import tobyspring.community.domain.comment.ModifyCommentRequest;
import tobyspring.community.domain.member.Member;
import tobyspring.community.domain.post.ModifyPostRequest;
import tobyspring.community.domain.post.Post;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentModifyService implements CommentRegister {
    private final CommentRepository commentRepository;
    private final MemberFinder memberFinder;
    private final PostFinder postFinder;
    private final CommentFinder commentFinder;

    @Override
    public CommentDetail create(Long postId, Long memberId, CreateCommentRequest createCommentRequest) {
        Member memberById = isActivateMemberValidation(memberId);

        Post postById = isPostedPostValidation(postId);

        if(createCommentRequest.parentCommentId() != null) checkParentComment(createCommentRequest);

        Comment comment = Comment.create(postId, memberId, createCommentRequest);

        commentRepository.save(comment);

        return CommentDetail.mapper(comment, memberById.getNickname(), postById.getTitle());
    }

    @Override
    public CommentDetail modify(Long postId, Long memberId, Long commentId, ModifyCommentRequest modifyPostRequest) {
        Member memberById = isActivateMemberValidation(memberId);

        Post postById = isPostedPostValidation(postId);

        Comment commentById = commentValidation(postId, memberId, commentId);

        commentById.modify(modifyPostRequest);

        commentRepository.save(commentById);

        return CommentDetail.mapper(commentById, memberById.getNickname(), postById.getTitle());
    }

    @Override
    public CommentDetail postComment(Long postId, Long memberId, Long commentId) {
        Member memberById = isActivateMemberValidation(memberId);

        Post postById = isPostedPostValidation(postId);

        Comment commentById = commentValidation(postId, memberId, commentId);

        commentById.postComment();

        commentRepository.save(commentById);

        return CommentDetail.mapper(commentById, memberById.getNickname(), postById.getTitle());
    }

    @Override
    public CommentDetail hide(Long postId, Long memberId, Long commentId) {
        Member memberById = isActivateMemberValidation(memberId);

        Post postById = isPostedPostValidation(postId);

        Comment commentById = commentValidation(postId, memberId, commentId);

        commentById.hide();

        commentRepository.save(commentById);

        return CommentDetail.mapper(commentById, memberById.getNickname(), postById.getTitle());
    }

    @Override
    public CommentDetail suspend(Long postId, Long memberId, Long commentId) {
        Member memberById = isActivateMemberValidation(memberId);

        Post postById = isPostedPostValidation(postId);

        Comment commentById = commentValidation(postId, memberId, commentId);

        commentById.suspend();

        commentRepository.save(commentById);

        return CommentDetail.mapper(commentById, memberById.getNickname(), postById.getTitle());
    }

    private Post isPostedPostValidation(Long postId) {
        Post postById = postFinder.findById(postId).post();
        if(postById.isPosted()) throw new NotPostedException("게시글");

        return postById;
    }

    private @NonNull Comment commentValidation(Long postId, Long memberId, Long commentId) {
        Comment commentById = commentFinder.getCommentById(commentId).comment();

        List<Comment> allByPostId = commentRepository.findAllByPostId(postId);
        if (!allByPostId.stream().map(Comment::getId).toList().contains(commentId))
            throw new NotFoundException("게시글에 해당하는 댓글을 찾을 수 없습니다.");

        if(!commentById.getMemberId().equals(memberId)) throw new NotAuthorException("작성자만 수정할 수 있습니다.");

        return commentById;
    }

    private @NonNull Member isActivateMemberValidation(Long memberId) {
        Member memberById = memberFinder.findById(memberId);
        if(!memberById.isActivate()) throw new NotActivatedUserException();
        return memberById;
    }

    private void checkParentComment(CreateCommentRequest createCommentRequest) {
        commentFinder.getCommentById(createCommentRequest.parentCommentId());
    }
}
