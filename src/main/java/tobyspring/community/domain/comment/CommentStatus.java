package tobyspring.community.domain.comment;

import jakarta.persistence.Embeddable;

@Embeddable
public enum CommentStatus {
    POSTED,
    SUSPENDED,
    HIDDEN
}
