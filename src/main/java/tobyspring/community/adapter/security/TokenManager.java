package tobyspring.community.adapter.security;

import tobyspring.community.domain.member.Member;
import tobyspring.community.domain.member.RegisterMemberRequest;
import tobyspring.community.domain.member.TokenResult;
import tobyspring.community.domain.shared.Email;

public interface TokenManager {
    TokenResult generateToken(Member request);

    String extractEmailFromToken(String token);

    Long extractMemberIdFromToken(String token);
}
