package tobyspring.community.application.post.provided;

import tobyspring.community.domain.post.CreatePostRequest;
import tobyspring.community.domain.post.ModifyPostRequest;
import tobyspring.community.domain.post.Post;

public interface PostRegister {
    Post post(CreatePostRequest request, Long memberId);

    Post modify(Long postId, Long memberId, ModifyPostRequest request);

    Post suspend(Long postId, Long memberId);
}
