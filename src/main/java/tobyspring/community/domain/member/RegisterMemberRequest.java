package tobyspring.community.domain.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterMemberRequest(
        @NotBlank(message = "email이 비었습니다.")
        @Email(message = "email 형식이 올바르지 않습니다.")
        String email,
        @Size(max = 10) String nickname,
        @Size(max = 15) String password
) {
}
