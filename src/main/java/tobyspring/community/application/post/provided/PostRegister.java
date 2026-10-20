package tobyspring.community.application.post.provided;

import tobyspring.community.domain.post.CreatePostRequest;
import tobyspring.community.domain.post.ModifyPostRequest;
import tobyspring.community.domain.post.Post;

public interface PostRegister {
    Post post(CreatePostRequest request, Long memberId);

    void modify(ModifyPostRequest request);

    void suspend();
}
