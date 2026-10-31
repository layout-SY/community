package tobyspring.community.adapter.webapi.comment;

import tobyspring.community.application.comment.provided.CommentDetail;
import tobyspring.community.domain.comment.Comment;
import tobyspring.community.domain.comment.CommentStatus;

import java.time.LocalDateTime;

public record CommentResponseDto(
        String content,
        String postTitle,
        String authorNickname,
        Long likes,
        Long parentCommentId,
        CommentStatus status,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {

    public static CommentResponseDto mapper(CommentDetail commentDetail) {
        return new CommentResponseDto(
                commentDetail.comment().getContent().content(),
                commentDetail.postTitle(),
                commentDetail.authorNickname(),
                commentDetail.comment().getLikes(),
                commentDetail.comment().getParentCommentId(),
                commentDetail.comment().getStatus(),
                commentDetail.comment().getCreatedAt(),
                commentDetail.comment().getModifiedAt()
        );
    }
}
