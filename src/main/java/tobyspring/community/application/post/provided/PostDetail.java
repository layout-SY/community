package tobyspring.community.application.post.provided;

import tobyspring.community.domain.post.Post;

public record PostDetail(
        Post post,
        String authorNickname
) {
    public static PostDetail mapper(Post post, String memberNickname) {
        return new PostDetail(
                post,
                memberNickname
        );
    }
}
