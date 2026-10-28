package tobyspring.community.domain.comment;

import java.util.Optional;

public record ModifyCommentRequest(
        String content,
        CommentStatus status
) {

    public boolean hasAnyUpdate() {
        return content != null || status != null;
    }
    public Optional<String> contentValue() {
        return Optional.ofNullable(content);
    }

    public Optional<CommentStatus> statusValue() {
        return Optional.ofNullable(status);
    }
}
