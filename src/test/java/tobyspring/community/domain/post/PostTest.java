package tobyspring.community.domain.post;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tobyspring.community.adapter.security.PasswordEncode;
import tobyspring.community.adapter.security.PasswordEncodeImpl;
import tobyspring.community.application.InvalidParameterException;
import tobyspring.community.domain.member.Member;
import tobyspring.community.domain.member.MemberFixture;

import java.util.ArrayList;
import java.util.List;

public class PostTest {
    Member member;
    PasswordEncode passwordEncode = new PasswordEncodeImpl();
    List<String> tags = PostFixture.getTags();

    @BeforeEach
    void setup() {
        this.member = Member.register(MemberFixture.getRegisterRequest(), passwordEncode);
    }

    @Nested
    @DisplayName("게시글 생성")
    class CreatePost {
        @Test
        void 성공() {
            Post post = Post.post(PostFixture.getCreatePostRequest(1L));

            Assertions.assertThat(post.getStatus()).isEqualTo(PostStatus.POSTED);
            Assertions.assertThat(post.getCreatedAt()).isNotNull();
            post.getTags().forEach(
                    tag -> Assertions.assertThat(tags.contains(tag.keyword())).isTrue()
            );
        }

        @Test
        void 성공_category_미설정() {
            Post post = Post.post(new CreatePostRequest("제목", 1L, "qwer", null, tags, null));

            Assertions.assertThat(post.getStatus()).isEqualTo(PostStatus.POSTED);
            Assertions.assertThat(post.getCreatedAt()).isNotNull();
            Assertions.assertThat(post.getCategory()).isEqualTo(PostCategory.ETC);
        }

        @Test
        void 실패_null_포함() {
            List<String> tags = PostFixture.getTags("java", "js", null, "jjj");

            Assertions.assertThatThrownBy(
                    () -> Post.post(PostFixture.getCreatePostRequest(1L, tags))
            ).isInstanceOf(NullPointerException.class);
        }

        @Test
        void 실패_필수_파라미터_누락() {
            Assertions.assertThatThrownBy(
                    () -> Post.post(new CreatePostRequest(null, 1L, "qwer", PostCategory.IT, tags, null))
            ).isInstanceOf(NullPointerException.class);

            Assertions.assertThatThrownBy(
                    () -> Post.post(new CreatePostRequest("제목", 1L, null, PostCategory.IT, tags, null))
            ).isInstanceOf(NullPointerException.class);
        }
    }
}
