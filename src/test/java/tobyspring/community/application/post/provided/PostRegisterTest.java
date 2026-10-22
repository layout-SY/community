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
import tobyspring.community.application.member.exception.NotActivatedUserException;
import tobyspring.community.application.member.exception.NotFoundException;
import tobyspring.community.application.member.provided.MemberRegister;
import tobyspring.community.application.post.exception.NotAuthorException;
import tobyspring.community.application.post.required.PostRepository;
import tobyspring.community.domain.member.Member;
import tobyspring.community.domain.member.MemberFixture;
import tobyspring.community.domain.member.MemberStatus;
import tobyspring.community.domain.post.*;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class PostRegisterTest {
    private final MemberRegister memberRegister;
    private final PostRegister postRegister;
    private final PostRepository postRepository;
    private final PostFinder postFinder;

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
            Assertions.assertThat(savedPost.getMemberId()).isEqualTo(post.getMemberId());
        }

        @Test
        void 실패_400_member가_activate_아님() {
            member = memberRegister.register(MemberFixture.getRegisterRequest("qazwsx@naver.com"));

            notMemberActivatedValidation();
        }
    }

    @Nested
    @DisplayName("게시글 수정")
    class ModifyPost {

        Post post;

        @BeforeEach
        void setup() {
            post = postRegister.post(PostFixture.getCreatePostRequest(), member.getId());
        }

        @Test
        void 성공() {
            Post modifiedPost = postRegister.modify(post.getId(), post.getMemberId(), PostFixture.getModifyPostRequest());

            Assertions.assertThat(modifiedPost.getModifiedAt()).isNotNull();
            Assertions.assertThat(modifiedPost.getTags())
                    .extracting("keyword")
                    .containsExactly("jaja", "gogo", "reset");
        }

        @Test
        void 성공_요청_body_내부의_null_포함_title() {
            ArrayList<String> imageUrls = new ArrayList<>();
            imageUrls.add("www.mimi.com");

            String previousValue = post.getTitle();

            Post modifiedPost = postRegister.modify(post.getId(), member.getId(), new ModifyPostRequest(
                    null, "테스트를 테스트합니다.", PostCategory.VACATION, PostFixture.getTags("cucu", "coco"), imageUrls
            ));

            Assertions.assertThat(modifiedPost.getTitle()).isEqualTo(previousValue);
            Assertions.assertThat(post)
                    .extracting(Post::getContent, Post::getCategory)
                    .containsExactly("테스트를 테스트합니다.", PostCategory.VACATION);
            Assertions.assertThat(post.getTags())
                    .extracting("keyword")
                    .containsExactly("cucu", "coco");
            Assertions.assertThat(post.getImageUrl())
                    .containsExactly("www.mimi.com");
        }

        @Test
        void 성공_요청_body_내부의_null_포함_content() {
            ArrayList<String> imageUrls = new ArrayList<>();
            imageUrls.add("www.mimiqw.com");

            String previousValue = post.getContent();

            Post modifiedPost = postRegister.modify(post.getId(), member.getId(), new ModifyPostRequest(
                    "테스트를 테스트", null, PostCategory.SOCIAL, PostFixture.getTags("zuzu", "zozo"), imageUrls
            ));

            Assertions.assertThat(modifiedPost.getContent()).isEqualTo(previousValue);
            Assertions.assertThat(post)
                    .extracting(Post::getTitle, Post::getCategory)
                    .containsExactly("테스트를 테스트", PostCategory.SOCIAL);
            Assertions.assertThat(post.getTags())
                    .extracting("keyword")
                    .containsExactly("zuzu", "zozo");
            Assertions.assertThat(post.getImageUrl())
                    .containsExactly("www.mimiqw.com");
        }

        @Test
        void 성공_요청_body_내부의_null_포함_category() {
            ArrayList<String> imageUrls = new ArrayList<>();
            imageUrls.add("www.mimiqw.com");

            PostCategory previousValue = post.getCategory();

            Post modifiedPost = postRegister.modify(post.getId(), member.getId(), new ModifyPostRequest(
                    "테스트를2 테스트", "테스트를 테스트 할까요?", null, PostFixture.getTags("zuzu", "zozo"), imageUrls
            ));

            Assertions.assertThat(modifiedPost.getCategory()).isEqualTo(previousValue);
            Assertions.assertThat(post)
                    .extracting(Post::getTitle, Post::getContent)
                    .containsExactly("테스트를2 테스트", "테스트를 테스트 할까요?");
            Assertions.assertThat(post.getTags())
                    .extracting("keyword")
                    .containsExactly("zuzu", "zozo");
            Assertions.assertThat(post.getImageUrl())
                    .containsExactly("www.mimiqw.com");
        }

        @Test
        void 성공_요청_body_내부의_null_포함_tags() {
            ArrayList<String> imageUrls = new ArrayList<>();
            imageUrls.add("www.mimiqw.com");

            List<Tag> previousValue = new ArrayList<>(post.getTags());

            Post modifiedPost = postRegister.modify(post.getId(), member.getId(), new ModifyPostRequest(
                    "테스트를2 테스트", "null 테스트", PostCategory.FASHION, null, imageUrls
            ));

            Assertions.assertThat(modifiedPost.getTags())
                    .containsExactlyElementsOf(previousValue);
            Assertions.assertThat(post)
                    .extracting(Post::getTitle, Post::getContent, Post::getCategory
                    )
                    .containsExactly("테스트를2 테스트", "null 테스트", PostCategory.FASHION);
            Assertions.assertThat(post.getImageUrl())
                    .containsExactly("www.mimiqw.com");
        }

        @Test
        void 성공_요청_body_내부의_null_포함_imageUrls() {
            ArrayList<String> imageUrls = new ArrayList<>();
            imageUrls.add("www.mimiqw.com");

            List<String> previousValue = new ArrayList<>(post.getImageUrl());

            Post modifiedPost = postRegister.modify(post.getId(), member.getId(), new ModifyPostRequest(
                    "테스트를1 테스트", "null 테스트2", PostCategory.FASHION, PostFixture.getTags("zongzong", "zingzing"), null
            ));

            Assertions.assertThat(modifiedPost.getImageUrl())
                    .containsExactlyElementsOf(previousValue);
            Assertions.assertThat(post)
                    .extracting(Post::getTitle, Post::getContent, Post::getCategory
                    )
                    .containsExactly("테스트를1 테스트", "null 테스트2", PostCategory.FASHION);
            Assertions.assertThat(post.getTags())
                    .extracting("keyword")
                    .containsExactly("zongzong", "zingzing");
        }

        @Test
        void 실패_404_없는_게시글() {
            Assertions.assertThatThrownBy(
                    () -> postRegister.modify(100L, member.getId(), PostFixture.getModifyPostRequest())
            ).isInstanceOf(NotFoundException.class);
        }

        @Test
        void 실패_400_작성자_외에_수정_요청() {
            Member registeredMember = memberRegister.register(MemberFixture.getRegisterRequest("qdgqdg@naver.com"));
            registeredMember.activate();

            Assertions.assertThatThrownBy(
                    () -> postRegister.modify(post.getId(), registeredMember.getId(), PostFixture.getModifyPostRequest())
            ).isInstanceOf(NotAuthorException.class);
        }


        @Test
        void 실패_400_member가_activate_아님() {
            member = memberRegister.register(MemberFixture.getRegisterRequest("qazwsx@naver.com"));

            notMemberActivatedValidation();
        }
    }

    @Nested
    @DisplayName("게시글 정지")
    class SuspendedPost {
        Post post;

        @BeforeEach
        void setup() {
            post = postRegister.post(PostFixture.getCreatePostRequest(), member.getId());
        }

        @Test
        void 성공() {
            post.suspend();

            Post postById = postFinder.findById(post.getId());

            Assertions.assertThat(postById.getStatus()).isEqualTo(PostStatus.SUSPENDED);
        }

        @Test
        void 실패_400_POSTED_외에_다른_상태() {
        }
    }

    private void notMemberActivatedValidation() {
        Assertions.assertThat(member.getStatus()).isEqualTo(MemberStatus.PENDING);
        Assertions.assertThatThrownBy(
                () -> postRegister.post(PostFixture.getCreatePostRequest(), member.getId())
        ).isInstanceOf(NotActivatedUserException.class);

        member.deactivate();
        Assertions.assertThat(member.getStatus()).isEqualTo(MemberStatus.DEACTIVATED);
        Assertions.assertThatThrownBy(
                () -> postRegister.post(PostFixture.getCreatePostRequest(), member.getId())
        ).isInstanceOf(NotActivatedUserException.class);
    }
}