package tobyspring.community.adapter.security;

public class TokenExtractor {
    private static final String BEARER_SCHEME = "Bearer";

    public static String extractToken(String authorizationHeader) {
        if (authorizationHeader == null) {
            throw new AuthenticateException("Authorization 헤더가 올바르지 않습니다");
        }
        String trimmed = authorizationHeader.trim();
        if (!trimmed.regionMatches(true, 0, BEARER_SCHEME, 0, BEARER_SCHEME.length())) {
            throw new AuthenticateException("Authorization 헤더가 올바르지 않습니다");
        }
        int spaceIdx = trimmed.indexOf(' ');
        if (spaceIdx < 0) {
            throw new AuthenticateException("Authorization 헤더가 올바르지 않습니다");
        }
        String token = trimmed.substring(spaceIdx + 1).trim();
        if (token.isEmpty()) {
            throw new AuthenticateException("Authorization 헤더가 올바르지 않습니다");
        }
        return token;
    }
}
