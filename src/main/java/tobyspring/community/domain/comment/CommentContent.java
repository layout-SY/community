package tobyspring.community.domain.comment;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static java.util.Objects.requireNonNull;
import static tobyspring.community.domain.shared.RequestValidator.lengthValidator;
import static tobyspring.community.domain.shared.RequestValidator.nullValidator;

@Embeddable
public record CommentContent(String content) {

    public CommentContent(String content) {
        nullValidator(content, "댓글");
        lengthValidator(content, "댓글", 100L);

        this.content = content;
    }
}
