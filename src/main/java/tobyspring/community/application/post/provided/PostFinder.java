package tobyspring.community.application.post.provided;

import tobyspring.community.domain.post.Post;

public interface PostFinder {
    Post findById(Long postId);

    Post findByMemberId(Long memberId);
}
