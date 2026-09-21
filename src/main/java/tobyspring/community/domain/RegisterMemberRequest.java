package tobyspring.community.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record RegisterMemberRequest(
        @Email String email,
        @Size(max = 10) String nickname,
        @Size(max = 15) String password
) {
}
