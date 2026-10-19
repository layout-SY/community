package tobyspring.community.domain.post;

import jakarta.validation.constraints.Size;

import java.util.*;
import java.util.stream.Collectors;

public record ModifyPostRequest(
        @Size(min = 1, max = 15) String title,
        @Size(min = 1, max = 200) String content,
        PostCategory category,
        List<String> tags,
        List<String> imageUrls
) {
    public ModifyPostRequest {
        tags = sanitize(tags);
        imageUrls = sanitize(imageUrls);
    }

    public Optional<String> titleValue() {
        return Optional.ofNullable(title);
    }

    public Optional<String> contentValue() {
        return Optional.ofNullable(content);
    }

    public Optional<PostCategory> categoryValue() {
        return Optional.ofNullable(category);
    }

    public Optional<List<String>> tagsValue() {
        return Optional.ofNullable(tags);
    }

    public Optional<List<String>> imageUrlsValue() {
        return Optional.ofNullable(imageUrls);
    }

    private static List<String> sanitize(List<String> value) {
        if (value == null) return null;
        return value.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toList();
    }
}
