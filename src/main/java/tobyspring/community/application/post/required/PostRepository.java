package tobyspring.community.application.post.required;

import org.springframework.data.repository.Repository;
import tobyspring.community.domain.post.Post;

import java.util.Optional;

public interface PostRepository extends Repository<Post, Long> {
    Post save(Post post);

    Optional<Post> findById(Long id);
}
