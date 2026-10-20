package tobyspring.community.application.post;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tobyspring.community.application.member.exception.NotActivatedUserException;
import tobyspring.community.application.member.provided.MemberFinder;
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
    public void modify(ModifyPostRequest request) {

    }

    @Override
    public void suspend() {

    }
}
