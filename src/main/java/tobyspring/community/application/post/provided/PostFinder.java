package tobyspring.community.application.post.provided;

import tobyspring.community.domain.post.Post;

import java.util.Collection;
import java.util.Map;

public interface PostFinder {
    PostDetail findById(Long postId);

    PostDetail findByMemberId(Long memberId);

    Map<Long, String> getTitleByPostIds(Collection<Long> postIds);
}
