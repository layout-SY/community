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

    public void modify(ModifyPostRequest request) {
        requireNonNull(request);

        title = request.titleValue().orElse(this.title);
        content = request.contentValue().orElse(this.content);
        category = request.categoryValue().orElse(this.category);
        request.tagsValue().ifPresent(list ->
                tags = list.stream()
                        .map(Tag::new)
                        .collect(Collectors.toCollection(ArrayList::new)));

        imageUrl = request.imageUrlsValue().orElse(this.imageUrl);

        modifiedAt = LocalDateTime.now();
    }

    public void like() {
        likes = this.likes + 1;
    }

    public void unlike() {
        likes = this.likes - 1;
    }

    public void suspendPost() {
        status = PostStatus.SUSPENDED;
    }

}
