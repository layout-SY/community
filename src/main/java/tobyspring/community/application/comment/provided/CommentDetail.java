package tobyspring.community.application.comment.provided;

import tobyspring.community.domain.comment.Comment;
import tobyspring.community.domain.post.Post;

public record CommentDetail(
        Comment comment,
        String authorNickname,
        String postTitle
) {

    public static CommentDetail mapper(Comment comment, String authorNickname, String postTitle) {
        return new CommentDetail(comment, authorNickname, postTitle);
    }
}
