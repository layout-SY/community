package tobyspring.community.application.post.provided;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import tobyspring.community.application.member.provided.MemberRegister;
import tobyspring.community.application.post.required.PostRepository;
import tobyspring.community.domain.member.Member;
import tobyspring.community.domain.member.MemberFixture;
import tobyspring.community.domain.post.Post;
import tobyspring.community.domain.post.PostFixture;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class PostRegisterTest {
    private final MemberRegister memberRegister;
    private final PostRegister postRegister;
    private final PostRepository postRepository;

    Member member;

    @BeforeEach
    void setup() {
        member = memberRegister.register(MemberFixture.getRegisterRequest());
        member.activate();
    }

    @Nested
    @DisplayName("게시글 생성")
    class Posting {
        @Test
        void 성공() {
            Post post = postRegister.post(PostFixture.getCreatePostRequest(), member.getId());

            Post savedPost = postRepository.findById(post.getId()).orElseThrow();

            Assertions.assertThat(savedPost.getCreatedAt()).isNotNull();
            Assertions.assertThat(savedPost.getMemberId()).isEqualTo(1);
        }
    }

    @Test
    void post() {
    }

    @Test
    void modify() {
    }

    @Test
    void suspend() {
    }
}