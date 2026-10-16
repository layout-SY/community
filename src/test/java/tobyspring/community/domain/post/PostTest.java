package tobyspring.community.domain.post;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tobyspring.community.adapter.security.PasswordEncode;
import tobyspring.community.adapter.security.PasswordEncodeImpl;
import tobyspring.community.domain.member.Member;
import tobyspring.community.domain.member.MemberFixture;

import java.util.ArrayList;
import java.util.List;

public class PostTest {

    @Nested
    @DisplayName("게시글")
    class Posting {
        Member member;
        PasswordEncode passwordEncode = new PasswordEncodeImpl();

        @BeforeEach
        void setup() {
            this.member = Member.register(MemberFixture.getRegisterRequest(), passwordEncode);
        }

        @Test
        void 성공() {
            Post post = Post.post(PostFixture.getCreatePostRequest(1L));
            List<String> tags = PostFixture.getTags();

            Assertions.assertThat(post.getStatus()).isEqualTo(PostStatus.POSTED);
            Assertions.assertThat(post.getCreatedAt()).isNotNull();
            post.getTags().forEach(
                    tag -> Assertions.assertThat(tags.contains(tag.keyword())).isTrue()
            );
        }
    }
}
