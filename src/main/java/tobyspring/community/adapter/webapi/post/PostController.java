package tobyspring.community.adapter.webapi.post;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import tobyspring.community.adapter.security.TokenExtractor;
import tobyspring.community.adapter.security.TokenManager;
import tobyspring.community.adapter.webapi.member.MemberDto;
import tobyspring.community.application.member.provided.MemberFinder;
import tobyspring.community.application.post.provided.PostDetail;
import tobyspring.community.application.post.provided.PostFinder;
import tobyspring.community.application.post.provided.PostRegister;
import tobyspring.community.domain.member.Member;
import tobyspring.community.domain.post.CreatePostRequest;
import tobyspring.community.domain.post.ModifyPostRequest;
import tobyspring.community.domain.post.Post;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PostController {
    private final PostRegister postRegister;
    private final TokenManager tokenManager;

    @PostMapping("/post")
    public PostResponseDto CreatePost(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION)
            String token,

            @RequestBody CreatePostRequest request
            ) {
        String extractedToken = TokenExtractor.extractToken(token);
        Long memberId = tokenManager.extractMemberIdFromToken(extractedToken);

        PostDetail postDetail = postRegister.post(request, memberId);

        return PostResponseDto.mapper(postDetail.post(), postDetail.authorNickname());
    }

    @GetMapping("/posts")
    public void getPosts() {
    }

    @GetMapping("/post/{id}")
    public void getPost(
            @PathVariable Long id
    ) {

    }

    @PutMapping("/post/{id}")
    public PostResponseDto modifyPost(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION)
            String token,

            @PathVariable Long postId,
            @RequestBody ModifyPostRequest request
            ) {
        String extractedToken = TokenExtractor.extractToken(token);
        Long memberId = tokenManager.extractMemberIdFromToken(extractedToken);

        PostDetail postDetail = postRegister.modify(postId, memberId, request);

        return PostResponseDto.mapper(postDetail.post(), postDetail.authorNickname());
    }
}
