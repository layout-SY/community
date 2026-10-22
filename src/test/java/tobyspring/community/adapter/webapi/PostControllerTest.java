package tobyspring.community.adapter.webapi;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tobyspring.community.TestFixture;
import tobyspring.community.adapter.security.TokenManager;
import tobyspring.community.application.member.required.MemberRepository;
import tobyspring.community.application.post.required.PostRepository;
import tobyspring.community.domain.post.Post;

@SpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@AutoConfigureMockMvc
@Transactional
@Import(TestFixture.class)
public class PostControllerTest {
    private final MockMvcTester mvcTester;
    private final PostRepository postRepository;
    private final TokenManager tokenManager;
    private final TestFixture testFixture;

    @Nested
    @DisplayName("게시글 생성")
    class Posting {
    }
}
