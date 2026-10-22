package tobyspring.community.application.post;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tobyspring.community.application.member.exception.NotActivatedUserException;
import tobyspring.community.application.member.provided.MemberFinder;
import tobyspring.community.application.post.exception.NotAuthorException;
import tobyspring.community.application.post.provided.PostFinder;
import tobyspring.community.application.post.provided.PostRegister;
import tobyspring.community.application.post.required.PostRepository;
import tobyspring.community.domain.member.Member;
import tobyspring.community.domain.post.CreatePostRequest;
import tobyspring.community.domain.post.ModifyPostRequest;
import tobyspring.community.domain.post.Post;

@Service
@RequiredArgsConstructor
public class PostModifyService implements PostRegister {
    private final MemberFinder memberFinder;
    private final PostFinder postFinder;
    private final PostRepository postRepository;

    @Override
    public Post post(CreatePostRequest request, Long memberId) {
        Member byId = memberFinder.findById(memberId);

        if(!byId.isActivate()) throw new NotActivatedUserException();

        Post post = Post.post(request, memberId);

        postRepository.save(post);

        return post;
    }

    @Override
    public Post modify(Long postId, Long memberId, ModifyPostRequest request) {
        Post postById = postFinder.findById(postId);

        Member memberById = memberFinder.findById(memberId);
        if(!memberById.isActivate()) throw new NotActivatedUserException();

        if(!memberId.equals(postById.getMemberId())) throw new NotAuthorException("작성자만 수정할 수 있습니다.");

        postById.modify(request);

        postRepository.save(postById);

        return postById;
    }

    @Override
    public Post suspend(Long postId, Long memberId) {
        Post postById = postFinder.findById(postId);

        Member memberById = memberFinder.findById(memberId);
        if(!memberById.isActivate()) throw new NotActivatedUserException();

        postById.suspend();

        postRepository.save(postById);

        return postById;
    }
}
