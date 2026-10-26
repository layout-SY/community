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
            PostDetail postDetail = postRegister.post(PostFixture.getCreatePostRequest(), member.getId());

            Post savedPost = postRepository.findById(postDetail.post().getId()).orElseThrow();

            Assertions.assertThat(savedPost.getCreatedAt()).isNotNull();
            Assertions.assertThat(savedPost.getMemberId()).isEqualTo(postDetail.post().getMemberId());
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

        PostDetail postDetail;

        @BeforeEach
        void setup() {
            postDetail = postRegister.post(PostFixture.getCreatePostRequest(), member.getId());
        }

        @Test
        void 성공() {
            PostDetail modifiedPost = postRegister.modify(postDetail.post().getId(), postDetail.post().getMemberId(), PostFixture.getModifyPostRequest());

            Assertions.assertThat(modifiedPost.post().getModifiedAt()).isNotNull();
            Assertions.assertThat(modifiedPost.post().getTags())
                    .extracting("keyword")
                    .containsExactly("jaja", "gogo", "reset");
        }

        @Test
        void 성공_요청_body_내부의_null_포함_title() {
            ArrayList<String> imageUrls = new ArrayList<>();
            imageUrls.add("www.mimi.com");

            String previousValue = postDetail.post().getTitle();

            PostDetail modifiedPost = postRegister.modify(postDetail.post().getId(), member.getId(), new ModifyPostRequest(
                    null, "테스트를 테스트합니다.", PostCategory.VACATION, PostFixture.getTags("cucu", "coco"), imageUrls
            ));

            Assertions.assertThat(modifiedPost.post().getTitle()).isEqualTo(previousValue);
            Assertions.assertThat(postDetail.post())
                    .extracting(Post::getContent, Post::getCategory)
                    .containsExactly("테스트를 테스트합니다.", PostCategory.VACATION);
            Assertions.assertThat(postDetail.post().getTags())
                    .extracting("keyword")
                    .containsExactly("cucu", "coco");
            Assertions.assertThat(postDetail.post().getImageUrls())
                    .containsExactly("www.mimi.com");
        }

        @Test
        void 성공_요청_body_내부의_null_포함_content() {
            ArrayList<String> imageUrls = new ArrayList<>();
            imageUrls.add("www.mimiqw.com");

            String previousValue = postDetail.post().getContent();

            PostDetail modifiedPost = postRegister.modify(postDetail.post().getId(), member.getId(), new ModifyPostRequest(
                    "테스트를 테스트", null, PostCategory.SOCIAL, PostFixture.getTags("zuzu", "zozo"), imageUrls
            ));

            Assertions.assertThat(modifiedPost.post().getContent()).isEqualTo(previousValue);
            Assertions.assertThat(postDetail.post())
                    .extracting(Post::getTitle, Post::getCategory)
                    .containsExactly("테스트를 테스트", PostCategory.SOCIAL);
            Assertions.assertThat(postDetail.post().getTags())
                    .extracting("keyword")
                    .containsExactly("zuzu", "zozo");
            Assertions.assertThat(postDetail.post().getImageUrls())
                    .containsExactly("www.mimiqw.com");
        }

        @Test
        void 성공_요청_body_내부의_null_포함_category() {
            ArrayList<String> imageUrls = new ArrayList<>();
            imageUrls.add("www.mimiqw.com");

            PostCategory previousValue = postDetail.post().getCategory();

            PostDetail modifiedPost = postRegister.modify(postDetail.post().getId(), member.getId(), new ModifyPostRequest(
                    "테스트를2 테스트", "테스트를 테스트 할까요?", null, PostFixture.getTags("zuzu", "zozo"), imageUrls
            ));

            Assertions.assertThat(modifiedPost.post().getCategory()).isEqualTo(previousValue);
            Assertions.assertThat(postDetail.post())
                    .extracting(Post::getTitle, Post::getContent)
                    .containsExactly("테스트를2 테스트", "테스트를 테스트 할까요?");
            Assertions.assertThat(postDetail.post().getTags())
                    .extracting("keyword")
                    .containsExactly("zuzu", "zozo");
            Assertions.assertThat(postDetail.post().getImageUrls())
                    .containsExactly("www.mimiqw.com");
        }

        @Test
        void 성공_요청_body_내부의_null_포함_tags() {
            ArrayList<String> imageUrls = new ArrayList<>();
            imageUrls.add("www.mimiqw.com");

            List<Tag> previousValue = new ArrayList<>(postDetail.post().getTags());

            PostDetail modifiedPost = postRegister.modify(postDetail.post().getId(), member.getId(), new ModifyPostRequest(
                    "테스트를2 테스트", "null 테스트", PostCategory.FASHION, null, imageUrls
            ));

            Assertions.assertThat(modifiedPost.post().getTags())
                    .containsExactlyElementsOf(previousValue);
            Assertions.assertThat(postDetail.post())
                    .extracting(Post::getTitle, Post::getContent, Post::getCategory
                    )
                    .containsExactly("테스트를2 테스트", "null 테스트", PostCategory.FASHION);
            Assertions.assertThat(postDetail.post().getImageUrls())
                    .containsExactly("www.mimiqw.com");
        }

        @Test
        void 성공_요청_body_내부의_null_포함_imageUrls() {
            ArrayList<String> imageUrls = new ArrayList<>();
            imageUrls.add("www.mimiqw.com");

            List<String> previousValue = new ArrayList<>(postDetail.post().getImageUrls());

            PostDetail modifiedPost = postRegister.modify(postDetail.post().getId(), member.getId(), new ModifyPostRequest(
                    "테스트를1 테스트", "null 테스트2", PostCategory.FASHION, PostFixture.getTags("zongzong", "zingzing"), null
            ));

            Assertions.assertThat(modifiedPost.post().getImageUrls())
                    .containsExactlyElementsOf(previousValue);
            Assertions.assertThat(postDetail.post())
                    .extracting(Post::getTitle, Post::getContent, Post::getCategory
                    )
                    .containsExactly("테스트를1 테스트", "null 테스트2", PostCategory.FASHION);
            Assertions.assertThat(postDetail.post().getTags())
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
        void 실패_403_작성자_외에_수정_요청() {
            Member registeredMember = memberRegister.register(MemberFixture.getRegisterRequest("qdgqdg@naver.com"));
            registeredMember.activate();

            Assertions.assertThatThrownBy(
                    () -> postRegister.modify(postDetail.post().getId(), registeredMember.getId(), PostFixture.getModifyPostRequest())
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
        PostDetail postDetail;

        @BeforeEach
        void setup() {
            postDetail = postRegister.post(PostFixture.getCreatePostRequest(), member.getId());
        }

        @Test
        void 성공() {
            postDetail.post().suspend();

            PostDetail postById = postFinder.findById(postDetail.post().getId());

            Assertions.assertThat(postById.post().getStatus()).isEqualTo(PostStatus.SUSPENDED);
        }

        @Test
        void 실패_400_POSTED_외에_다른_상태() {
        }
    }

    @Nested
    @DisplayName("게시글 삭제")
    class DeletingPost {
        PostDetail postDetail;

        @BeforeEach
        void setup() {
            postDetail = postRegister.post(PostFixture.getCreatePostRequest(), member.getId());
        }

        @Test
        void 성공() {
            postRegister.delete(postDetail.post().getId(), member.getId());

            Assertions.assertThatThrownBy(
                    () -> postFinder.findById(postDetail.post().getId())
            ).isInstanceOf(NotFoundException.class);
        }

        @Test
        void 실패_403_작성자_외에_삭제_요청() {
            Member registered = memberRegister.register(MemberFixture.getRegisterRequest("endfn@naver.com"));

            Assertions.assertThatThrownBy(
                    () -> postRegister.delete(postDetail.post().getId(), registered.getId())
            ).isInstanceOf(NotAuthorException.class);
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