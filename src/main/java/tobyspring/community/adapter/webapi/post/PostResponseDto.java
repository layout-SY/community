package tobyspring.community.adapter.webapi.post;

import tobyspring.community.adapter.webapi.member.MemberDto;
import tobyspring.community.domain.post.Post;
import tobyspring.community.domain.post.PostCategory;
import tobyspring.community.domain.post.PostStatus;
import tobyspring.community.domain.post.Tag;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record PostResponseDto(
        Long postId,
        String title,
        String content,
        String memberNickname,
        PostCategory category,
        PostStatus status,
        Long likes,
        List<String> tags,
        List<String> imageUrls,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {

    public static PostResponseDto mapper(Post post, String memberNickname) {
        return new PostResponseDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                memberNickname,
                post.getCategory(),
                post.getStatus(),
                post.getLikes(),
                post.getTags().stream().map(Tag::keyword).collect(Collectors.toCollection(ArrayList::new)),
                post.getImageUrl(),
                post.getCreatedAt(),
                post.getCreatedAt()
        );
    }
}
