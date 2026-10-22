package tobyspring.community.application.post;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tobyspring.community.application.member.exception.NotFoundException;
import tobyspring.community.application.post.provided.PostFinder;
import tobyspring.community.application.post.required.PostRepository;
import tobyspring.community.domain.post.Post;

@Service
@RequiredArgsConstructor
public class PostQueryService implements PostFinder {
    private final PostRepository postRepository;

    @Override
    public Post findById(Long postId) {
        return postRepository.findById(postId).orElseThrow(
                () -> new NotFoundException("없는 게시글입니다.")
        );
    }

    @Override
    public Post findByMemberId(Long memberId) {
        return null;
    }
}
