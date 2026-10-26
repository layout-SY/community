package tobyspring.community.application.post;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tobyspring.community.application.member.exception.NotFoundException;
import tobyspring.community.application.member.provided.MemberFinder;
import tobyspring.community.application.post.provided.PostDetail;
import tobyspring.community.application.post.provided.PostFinder;
import tobyspring.community.application.post.required.PostRepository;
import tobyspring.community.domain.member.Member;
import tobyspring.community.domain.post.Post;

@Service
@RequiredArgsConstructor
public class PostQueryService implements PostFinder {
    private final PostRepository postRepository;
    private final MemberFinder memberFinder;

    @Override
    public PostDetail findById(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new NotFoundException("없는 게시글입니다.")
        );

        Member memberById = memberFinder.findById(post.getMemberId());

        return PostDetail.mapper(post, memberById.getNickname());
    }

    @Override
    public PostDetail findByMemberId(Long memberId) {
        return null;
    }
}
