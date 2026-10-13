package tobyspring.community;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.ObjectMapper;

@TestConfiguration
public class TestFixtureConfig {

    @Bean
    TestFixture testFixture(ObjectMapper objectMapper) {
        return new TestFixture(objectMapper);
    }
}
