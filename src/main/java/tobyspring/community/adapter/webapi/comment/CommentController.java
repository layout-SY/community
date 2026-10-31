package tobyspring.community.adapter.webapi.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;
import tobyspring.community.adapter.security.TokenExtractor;
import tobyspring.community.adapter.security.TokenManager;
import tobyspring.community.application.comment.provided.CommentDetail;
import tobyspring.community.application.comment.provided.CommentFinder;
import tobyspring.community.application.comment.provided.CommentRegister;
import tobyspring.community.application.member.provided.MemberFinder;
import tobyspring.community.application.member.provided.MemberRegister;
import tobyspring.community.application.post.provided.PostFinder;
import tobyspring.community.application.post.provided.PostRegister;
import tobyspring.community.domain.comment.CommentStatus;
import tobyspring.community.domain.comment.CreateCommentRequest;
import tobyspring.community.domain.comment.ModifyCommentRequest;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CommentController {
    private final CommentFinder commentFinder;
    private final CommentRegister commentRegister;
    private final TokenManager tokenManager;

    @PostMapping("/{postId}")
    public CommentResponseDto createComment(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @PathVariable Long postId,
            @RequestBody CreateCommentRequest request
    ) {
        String extractedToken = TokenExtractor.extractToken(token);
        Long memberId = tokenManager.extractMemberIdFromToken(extractedToken);

        CommentDetail commentDetail = commentRegister.create(postId, memberId, request);

        return CommentResponseDto.mapper(commentDetail);
    }

    @PutMapping("/{postId}/{commentId}")
    public CommentResponseDto modifyComment(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody ModifyCommentRequest request
    ) {
        String extractedToken = TokenExtractor.extractToken(token);
        Long memberId = tokenManager.extractMemberIdFromToken(extractedToken);

        CommentDetail commentDetail = commentRegister.modify(postId, memberId, commentId, request);

        return CommentResponseDto.mapper(commentDetail);

    }

    @PutMapping("/{postId}/{commentId}/{status}")
    public CommentResponseDto changeCommentStatus(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @PathVariable CommentStatus status
    ) {
        String extractedToken = TokenExtractor.extractToken(token);
        Long memberId = tokenManager.extractMemberIdFromToken(extractedToken);

        CommentDetail commentDetail = commentRegister.changeStatus(postId, memberId, commentId, status);

        return CommentResponseDto.mapper(commentDetail);
    }

    @DeleteMapping("/{postId}/{commentId}")
    public boolean deleteComment(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        String extractedToken = TokenExtractor.extractToken(token);
        Long memberId = tokenManager.extractMemberIdFromToken(extractedToken);

        return commentRegister.delete(postId, memberId, commentId);
    }

    @GetMapping("/{postId}")
    public List<CommentResponseDto> getComments(
            @PathVariable Long postId
    ) {
        List<CommentDetail> commentDetailsByPostId = commentFinder.getCommentsByPostId(postId);

        return commentDetailsByPostId.stream().map(CommentResponseDto::mapper).collect(Collectors.toList());
    }
}
