package tobyspring.community.domain.post;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PostFixture {
    public static List<String> getTags() {
        return getTags("java", "element", "todo");
    }

    public static List<String> getTags(String... tag) {
        return Arrays.asList(tag);
    }

    public static CreatePostRequest getCreatePostRequest(Long memberId) {
        return getCreatePostRequest(memberId, getTags());
    }

    public static CreatePostRequest getCreatePostRequest(Long memberId, List<String> tags) {
       return new CreatePostRequest("테스트", memberId, "본문 내용입니다.", PostCategory.IT, tags, null);
    }
}
