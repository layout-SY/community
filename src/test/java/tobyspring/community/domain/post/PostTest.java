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
import java.util.Collections;
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
    @DisplayName("게시글 관련 별도 테스트")
    class Etc {
        Post post;

        @BeforeEach
        void setup() {
            post = Post.post(PostFixture.getCreatePostRequest(1L));
            Assertions.assertThat(post.getStatus()).isEqualTo(PostStatus.POSTED);
        }

        @Test
        void 실패_길이_제한() {
            Assertions.assertThatThrownBy(
                    () -> post.modify(new ModifyPostRequest("a".repeat(16), null, null, null, null))
            ).isInstanceOf(IllegalArgumentException.class);

            Assertions.assertThatThrownBy(
                    () -> post.modify(new ModifyPostRequest(null, "a".repeat(201), null, null, null))
            ).isInstanceOf(IllegalArgumentException.class);
        }
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

    @Nested
    @DisplayName("게시글 수정")
    class ModifyPost {

        Post post;

        @BeforeEach
        void setup() {
            post = Post.post(PostFixture.getCreatePostRequest(1L));
            Assertions.assertThat(post.getStatus()).isEqualTo(PostStatus.POSTED);
        }

        @Test
        void 성공() {
            post.modify(PostFixture.getModifyPostRequest(1L));

            Assertions.assertThat(post.getCreatedAt()).isNotNull();
            Assertions.assertThat(post.getModifiedAt()).isNotNull();
            Assertions.assertThat(post.getTitle()).isEqualTo("수정된 제목");
            Assertions.assertThat(post.getTags()).contains(new Tag("jaja"), new Tag("gogo"), new Tag("reset"), new Tag("reset"));
        }

        @Test
        void 성공_null_필드는_원본_값_유지() {
            post.modify(new ModifyPostRequest(null, null, null, null, null));

            Assertions.assertThat(post)
                    .extracting(Post::getTitle, Post::getContent, Post::getCategory)
                    .containsExactly("테스트", "본문 내용입니다.", PostCategory.IT);

            Assertions.assertThat(post.getTags()).contains(new Tag("java"), new Tag("element"), new Tag("todo"));
            Assertions.assertThat(post.getImageUrl()).contains("https://www.google.com");
        }

        @Test
        void 성공_null_필드가_일부만_있을_경우_title_content_category() {
            post.modify(new ModifyPostRequest(null, "content 필드 null 확인", PostCategory.VACATION, null, null));

            Assertions.assertThat(post)
                    .extracting(Post::getTitle, Post::getContent, Post::getCategory)
                    .containsExactly("테스트", "content 필드 null 확인", PostCategory.VACATION);

            Assertions.assertThat(post.getTags()).contains(new Tag("java"), new Tag("element"), new Tag("todo"));
            Assertions.assertThat(post.getImageUrl()).contains("https://www.google.com");
        }

        @Test
        void 성공_null_필드가_일부만_있을_경우_tags_imageUrls() {
            ArrayList<String> imageUrls = new ArrayList<>();
            imageUrls.add("https://www.naver.com");

            post.modify(new ModifyPostRequest(null, null, PostCategory.VACATION, PostFixture.getTags("jojo", "juju"), imageUrls));

            Assertions.assertThat(post)
                    .extracting(Post::getTitle, Post::getContent, Post::getCategory)
                    .containsExactly("테스트", "본문 내용입니다.", PostCategory.VACATION);

            Assertions.assertThat(post.getTags()).contains(new Tag("jojo"), new Tag("juju"));
            Assertions.assertThat(post.getImageUrl()).contains("https://www.naver.com");
        }

        @Test
        void 실패_null이_포함된_list() {
            ArrayList<String> imageUrls = new ArrayList<>();
            imageUrls.add("https://www.naver.com");
            imageUrls.add(null);
            imageUrls.add("https://www.jij.com");

            post.modify(PostFixture.getModifyPostRequest(1L, PostFixture.getTags("jiji", null, "jogjog")));

            Assertions.assertThat(post.getTags()).hasSize(2);
            Assertions.assertThat(post.getTags()).containsExactly(new Tag("jiji"), new Tag("jogjog"));

            post.modify(new ModifyPostRequest(null, null, null, null, imageUrls));

            Assertions.assertThat(post.getTags()).hasSize(2);
            Assertions.assertThat(post.getImageUrl()).containsExactly("https://www.naver.com", "https://www.jij.com");

        }

        @Test
        void 실패_요청_body_null() {
            Assertions.assertThatThrownBy(
                    () -> post.modify(null)
            ).isInstanceOf(NullPointerException.class);
        }
    }
}
