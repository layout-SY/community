package tobyspring.community.application.post.provided;

import tobyspring.community.domain.post.Post;

public interface PostFinder {
    PostDetail findById(Long postId);

    PostDetail findByMemberId(Long memberId);
}
