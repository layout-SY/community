package tobyspring.community.domain.member;

import jakarta.persistence.Embeddable;

import java.util.regex.Pattern;

import static org.springframework.util.Assert.state;

@Embeddable
public record Profile(String profile_address) {

    private static final Pattern PROFILE_PATTERN =
            Pattern.compile("[a-z0-9]+");

    public Profile {
        state(PROFILE_PATTERN.matcher(profile_address).matches(), "프로필 패턴이 일치하지 않습니다. : " + profile_address);

        state(profile_address.length() < 15, "프로필 주소는 최대 15자리를 넘을 수 없습니다. : " + profile_address);
    }

    public String url() {
        return "@" + profile_address;
    }
}
