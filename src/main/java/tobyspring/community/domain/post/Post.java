package tobyspring.community.domain.post;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tobyspring.community.domain.EntityAbstract;

import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@NoArgsConstructor
@Entity
@Table
@Getter
@ToString
public class Post extends EntityAbstract {

    private String title;

    private Long memberId;

    private String content;

    @Enumerated(EnumType.STRING)
    private PostCategory category;

    @Enumerated(EnumType.STRING)
    private PostStatus status;

    private Long likes;

    private List<Tag> tags;

    private List<String> imageUrl;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    public static Post post(CreatePostRequest request) {
        Post post = new Post();

        post.title = requireNonNull(request.title());
        post.content = requireNonNull(request.content());
        post.memberId = requireNonNull(request.memberId());
        post.category = requireNonNull(request.category());
        post.imageUrl = request.imageUrls();
        if (request.tags() != null && !request.tags().isEmpty()) {
            post.tags = request.tags().stream()
                    .map(Tag::new)
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        post.status = PostStatus.POSTED;
        post.createdAt = LocalDateTime.now();

        return post;
    }

    public Post modify(Post post, ModifyPostRequest request) {
        requireNonNull(request);

        post.title = request.title().orElse(this.title);
        post.content = request.content().orElse(this.content);
//        post.tags = request.tags().map(tags);

        post.modifiedAt = LocalDateTime.now();

        return post;
    }

    public Post like(Post post) {
        post.likes = post.likes + 1;

        return post;
    }

    public Post unlike(Post post) {
        post.likes = post.likes - 1;

        return post;
    }

    public void suspendPost(Post post) {
        post.status = PostStatus.SUSPENDED;
    }

}
