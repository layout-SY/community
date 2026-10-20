package tobyspring.community.domain.post;

import io.jsonwebtoken.lang.Assert;
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

    private Long likes = 0L;

    @ElementCollection
    @CollectionTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id"))
    private List<Tag> tags;

    @ElementCollection
    @CollectionTable(name = "post_images", joinColumns = @JoinColumn(name = "post_id"))
    private List<String> imageUrl;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    public static Post post(CreatePostRequest request, Long memberId) {
        Post post = new Post();

        requireNonNull(request, "요청이 null일 수 없습니다.");

        post.title = requireNonNull(request.title(), "제목은 필수 입력입니다. : " + request.title());
        post.content = requireNonNull(request.content(), "본문은 필수 입력입니다. : " + request.content());
        post.memberId = requireNonNull(memberId);
        post.category = request.category() == null ? PostCategory.ETC : request.category();
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
        requireNonNull(request, "요청 body가 null일 수 없습니다.");

        title = request.titleValue().map(Post::validateTitle).orElse(this.title);
        content = request.contentValue().map(Post::validateContent).orElse(this.content);
        category = request.categoryValue().orElse(this.category);
        request.tagsValue().ifPresent(list ->
                tags = list.stream()
                        .map(Tag::new)
                        .collect(Collectors.toCollection(ArrayList::new)));

        imageUrl = request.imageUrlsValue().orElse(this.imageUrl);

        modifiedAt = LocalDateTime.now();
    }

    public void like() {
        Assert.state(status == PostStatus.POSTED, "게시 중인 글만 좋아요할 수 있습니다.");
        likes = this.likes + 1;
    }

    public void unlike() {
        Assert.state(status == PostStatus.POSTED, "게시 중인 글만 좋아요할 수 있습니다.");
        likes = this.likes - 1;
    }

    public void suspend() {
        status = PostStatus.SUSPENDED;
    }

    private static String validateTitle(String title) {
        if(title.length() > 15) {
            throw new IllegalArgumentException("제목은 15자리를 넘길 수 없습니다.");
        }

        return title;
    }

    private static String validateContent(String content) {
        if(content.length() > 200) {
            throw new IllegalArgumentException("본문은 200자리를 넘길 수 없습니다.");
        }

        return content;
    }

}
