package tobyspring.community.domain.comment;

import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static tobyspring.community.domain.shared.RequestValidator.lengthValidator;
import static tobyspring.community.domain.shared.RequestValidator.nullValidator;

public record ModifyCommentRequest(
        String content,
        CommentStatus status
) {

    public ModifyCommentRequest(String content, CommentStatus status) {
        lengthValidator(content, "댓글", 100L);

        this.content = content;
        this.status = status;
    }

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
