package tobyspring.community.application.post.provided;

import tobyspring.community.adapter.webapi.post.PostResponseDto;
import tobyspring.community.domain.post.CreatePostRequest;
import tobyspring.community.domain.post.ModifyPostRequest;
import tobyspring.community.domain.post.Post;

public interface PostRegister {
    PostDetail post(CreatePostRequest request, Long memberId);

    PostDetail modify(Long postId, Long memberId, ModifyPostRequest request);

    PostDetail suspend(Long postId, Long memberId);

    boolean delete(Long postId, Long memberId);
}
