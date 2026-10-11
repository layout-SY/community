package tobyspring.community.adapter.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tobyspring.community.domain.member.Member;
import tobyspring.community.domain.member.RegisterMemberRequest;
import tobyspring.community.domain.member.TokenResult;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class TokenManagerImpl implements TokenManager{
    private final SecretKey key;
    private final long expiration = 24 * 60 * 60 * 1000;

    public TokenManagerImpl(@Value("${jwt.secret:myDefaultSecretKeyThatIsLongEnoughForHS256}") String secret) {
        // HMAC-SHA256은 최소 32바이트(256비트) 키 필요
        if (secret.length() < 32) {
            secret = secret + "0".repeat(32 - secret.length());
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    @Override
    public TokenResult generateToken(Member member) {
        String accessToken = Jwts.builder()
                .subject(member.getEmail().getAddress())
                .claim("memberId", member.getId())
                .claim("nickname", member.getNickname())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();

        String refreshToken = Jwts.builder()
                .subject(member.getEmail().getAddress())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration * 60))
                .signWith(key)
                .compact();

        return new TokenResult(accessToken, refreshToken);
    }

    @Override
    public String extractEmailFromToken(String token) {
        try{
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        }catch (JwtException ex) {
            throw new AuthenticateException("유효하지 않은 토큰 정보입니다.");
        }
    }

    @Override
    public Long extractMemberIdFromToken(String token) {

        Object memberId = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("memberId");

        if(memberId instanceof Integer) {
            return ((Integer) memberId).longValue();
        } else if (memberId instanceof Long) {
            return (Long) memberId;
        }

        throw new AuthenticateException("토큰에서 회원 ID를 찾을 수 없습니다");
    }
}
