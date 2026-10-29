package tobyspring.community.application.comment;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import tobyspring.community.application.comment.provided.CommentFinder;
import tobyspring.community.application.comment.provided.CommentRegister;
import tobyspring.community.application.comment.requried.CommentRepository;
import tobyspring.community.application.member.exception.NotActivatedUserException;
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

@Service
@RequiredArgsConstructor()
public class CommentModifyService implements CommentRegister {
    private final CommentRepository commentRepository;
    private final MemberFinder memberFinder;
    private final PostFinder postFinder;
    private final CommentFinder commentFinder;

    @Override
    public Comment create(Long postId, Long memberId, CreateCommentRequest createCommentRequest) {
        Member memberById = memberFinder.findById(memberId);
        if(memberById.isActivate()) throw new NotActivatedUserException();

        isPostedPostValidation(postId);

        Comment comment = Comment.create(postId, memberId, createCommentRequest);

        commentRepository.save(comment);

        return comment;
    }

    @Override
    public Comment modify(Long postId, Long memberId, Long commentId, ModifyCommentRequest modifyPostRequest) {
        isPostedPostValidation(postId);

        Comment commentById = commentValidation(memberId, commentId);

        commentById.modify(modifyPostRequest);

        commentRepository.save(commentById);

        return commentById;
    }

    @Override
    public Comment postComment(Long postId, Long memberId, Long commentId) {
        isPostedPostValidation(postId);

        Comment commentById = commentValidation(memberId, commentId);

        commentById.postComment();

        commentRepository.save(commentById);

        return commentById;
    }

    @Override
    public Comment hide(Long postId, Long memberId, Long commentId) {
        isPostedPostValidation(postId);

        Comment commentById = commentValidation(memberId, commentId);

        commentById.hide();

        commentRepository.save(commentById);

        return commentById;
    }

    @Override
    public Comment suspend(Long postId, Long memberId, Long commentId) {
        isPostedPostValidation(postId);

        Comment commentById = commentValidation(memberId, commentId);

        commentById.suspend();

        commentRepository.save(commentById);

        return commentById;
    }

    private void isPostedPostValidation(Long postId) {
        Post postById = postFinder.findById(postId).post();
        if(postById.isPosted()) throw new NotPostedException("게시글");
    }

    private @NonNull Comment commentValidation(Long memberId, Long commentId) {
        Comment commentById = commentFinder.getCommentById(commentId);

        if(!commentById.isPostedComment()) throw new NotPostedException("댓글");
        if(!commentById.getMemberId().equals(memberId)) throw new NotAuthorException("작성자만 수정할 수 있습니다.");
        return commentById;
    }
}
