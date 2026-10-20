package tobyspring.community.application.post;

import org.springframework.stereotype.Service;
import tobyspring.community.application.post.provided.PostFinder;
import tobyspring.community.domain.post.Post;

@Service
public class PostQueryService implements PostFinder {
    @Override
    public Post findById(Long postId) {
        return null;
    }

    @Override
    public Post findByMemberId(Long memberId) {
        return null;
    }
}
