package tobyspring.community.domain.post;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

public record CreatePostRequest(
   @Size(min = 1, max = 15) String title,
   Long memberId,
   @Size(min = 1, max = 200) String content,
   PostCategory category,
   List<String> tags,
   List<String> imageUrls
) {}
