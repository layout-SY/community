package tobyspring.community;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class TestFixture {
    private final ObjectMapper objectMapper;

    // 응답 변환 및 검증

    public String toJson(Object body) {
        return objectMapper.writeValueAsString(body);
    }

    public <T> T readBody(MvcTestResult exchange, Class<T> type) {
        return objectMapper.readValue(exchange.getResponse().getContentAsByteArray(), type);
    }

    public void assertProblem(MvcTestResult exchange, HttpStatus status, String detail) {
        assertThat(exchange).hasStatus(status);
        assertThat(readBody(exchange, ProblemDetail.class).getDetail()).isEqualTo(detail);
    }
}