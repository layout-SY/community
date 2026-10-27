package tobyspring.community.domain.comment;

import java.util.Optional;

public record ModifyCommentRequest(
        String content,
        CommentStatus status
) {

    public Optional<String> contentValue() {
        return Optional.ofNullable(content);
    }

    public Optional<CommentStatus> statusValue() {
        return Optional.ofNullable(status);
    }
}
