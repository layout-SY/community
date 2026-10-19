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
        ArrayList<String> imageUrls = new ArrayList<>();
        imageUrls.add("https://www.google.com");

       return new CreatePostRequest("테스트", memberId, "본문 내용입니다.", PostCategory.IT, tags, imageUrls);
    }

    public static ModifyPostRequest getModifyPostRequest(Long memberId) {
        return getModifyPostRequest(memberId, getTags("jaja", "gogo", "reset"));
    }

    public static ModifyPostRequest getModifyPostRequest(Long memberId, List<String> tags) {
        ArrayList<String> imageUrls = new ArrayList<>();
        imageUrls.add("https://www.naver.com");

        return new ModifyPostRequest("수정된 제목", "수정된 본문", PostCategory.COOK,  tags, imageUrls);
    }
}
