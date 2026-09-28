package tobyspring.community.domain.member;

import jakarta.validation.constraints.Size;

public record MemberDetailRequest(
        @Size(max = 15) String profile,
        @Size(max = 200) String introduction
) {
}
