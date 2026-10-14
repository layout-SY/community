package tobyspring.community.adapter.security;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class TokenExtractorTest {
    private final String BEARER_SCHEME = "Bearer";
    private static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
    private final String INVALID_HEADER_MESSAGE = "Authorization 헤더가 올바르지 않습니다";

    @Nested
    @DisplayName("정상 추출")
    class ExtractSuccess {
        @Test
        void 성공() {
            String tokenWithAuthorizationHeader = createBearerToken(VALID_TOKEN);

            String extractedToken = TokenExtractor.extractToken(tokenWithAuthorizationHeader);

            Assertions.assertThat(extractedToken).isEqualTo(VALID_TOKEN);
        }

        @ParameterizedTest
        @ValueSource(strings = {"Bearer", "bearer", "BEARER", "BeArEr"})
        void 접두사_Bearer_구분없이_성공(String bearerKey) {
            String tokenWithAuthorizationHeader = bearerKey + " " + VALID_TOKEN;

            String extractedToken = TokenExtractor.extractToken(tokenWithAuthorizationHeader);

            Assertions.assertThat(extractedToken).isEqualTo(VALID_TOKEN);
        }

        @Test
        void 앞뒤중간_공백크기에_상관없이_성공() {
            String tokenWithAuthorizationHeader = "   " + BEARER_SCHEME + "                 " + VALID_TOKEN + "      ";

            String extractedToken = TokenExtractor.extractToken(tokenWithAuthorizationHeader);

            Assertions.assertThat(extractedToken).isEqualTo(VALID_TOKEN);
        }
    }

    @Nested
    @DisplayName("추출 실패")
    class ExtractFail {
        @Test
        void 헤더_null() {
            Assertions.assertThatThrownBy(
                    () -> TokenExtractor.extractToken(null)
            ).isInstanceOf(AuthenticateException.class)
                    .hasMessage(INVALID_HEADER_MESSAGE);
        }

        @Test
        void Bearer_없음() {
            Assertions.assertThatThrownBy(
                    () -> TokenExtractor.extractToken(BEARER_SCHEME)
            ).isInstanceOf(AuthenticateException.class)
                    .hasMessage(INVALID_HEADER_MESSAGE);
        }

        @Test
        void Bearer_오타() {
            Assertions.assertThatThrownBy(
                    () -> TokenExtractor.extractToken("Bear")
            ).isInstanceOf(AuthenticateException.class)
                    .hasMessage(INVALID_HEADER_MESSAGE);
        }

        @Test
        void token이_없는_경우() {
            Assertions.assertThatThrownBy(
                            () -> TokenExtractor.extractToken("Bearer ")
                    ).isInstanceOf(AuthenticateException.class)
                    .hasMessage(INVALID_HEADER_MESSAGE);
        }

        @Test
        void Bearer_뒤_공백이_없는_경우() {
            Assertions.assertThatThrownBy(
                            () -> TokenExtractor.extractToken("Bearer" + VALID_TOKEN)
                    ).isInstanceOf(AuthenticateException.class)
                    .hasMessage(INVALID_HEADER_MESSAGE);
        }
    }

    private String createBearerToken(String token) {
        return BEARER_SCHEME + " " + token;
    }
}
