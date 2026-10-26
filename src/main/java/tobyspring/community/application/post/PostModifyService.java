package tobyspring.community.application.post;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tobyspring.community.application.member.exception.NotActivatedUserException;
import tobyspring.community.application.member.exception.NotFoundException;
import tobyspring.community.application.member.provided.MemberFinder;
import tobyspring.community.application.post.exception.NotAuthorException;
import tobyspring.community.application.post.provided.PostDetail;
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
    public PostDetail post(CreatePostRequest request, Long memberId) {
        Member byId = memberFinder.findById(memberId);

        if(!byId.isActivate()) throw new NotActivatedUserException();

        Post post = Post.post(request, memberId);

        postRepository.save(post);

        return PostDetail.mapper(post, byId.getNickname());
    }

    @Override
    public PostDetail modify(Long postId, Long memberId, ModifyPostRequest request) {
        PostDetail postById = postFinder.findById(postId);

        Member memberById = memberFinder.findById(memberId);
        if(!memberById.isActivate()) throw new NotActivatedUserException();

        if(!memberId.equals(postById.post().getMemberId())) throw new NotAuthorException("작성자만 수정할 수 있습니다.");

        postById.post().modify(request);

        postRepository.save(postById.post());

        return PostDetail.mapper(postById.post(), memberById.getNickname());
    }

    @Override
    public PostDetail suspend(Long postId, Long memberId) {
        PostDetail postById = postFinder.findById(postId);

        Member memberById = memberFinder.findById(memberId);
        if(!memberById.isActivate()) throw new NotActivatedUserException();

        postById.post().suspend();

        postRepository.save(postById.post());

        return PostDetail.mapper(postById.post(), memberById.getNickname());
    }

    @Override
    public boolean delete(Long postId, Long memberId) {
        PostDetail postById = postFinder.findById(postId);

        if(!memberId.equals(postById.post().getMemberId())) throw new NotAuthorException("작성자만 삭제할 수 있습니다.");

        postRepository.delete(postById.post());

        return true;
    }
}
