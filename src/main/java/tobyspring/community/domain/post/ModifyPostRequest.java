package tobyspring.community.domain.post;

import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

record ModifyPostRequest(
        @Size(min = 1, max = 15) Optional<String> title,
        @Size(min = 1, max = 200) Optional<String> content,
        Optional<List<String>> tags,
        Optional<List<String>> imageUrls
) {
    public ModifyPostRequest {
        title = verifyNull(title);
        content = verifyNull(content);
        tags = sanitize(tags);
        imageUrls = sanitize(imageUrls);
    }

    private Optional<String> verifyNull(Optional<String> value) {
        return value == null ? Optional.empty() : value;
    }

    private static Optional<List<String>> sanitize(Optional<List<String>> value) {
        if (value == null || value.isEmpty()) {
            return Optional.empty();
        }
        List<String> sanitized = value.get().stream()
                .map(tag -> tag == null ? null : tag.trim())
                .collect(Collectors.toCollection(ArrayList::new));
        return Optional.of(Collections.unmodifiableList(sanitized));
    }
}
