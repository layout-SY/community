package tobyspring.community.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import tobyspring.community.domain.member.Profile;

public class ProfileTest {
    @Test
    void 프로필_주소() {
        new Profile("qweasd");
    }

    @Test
    void 프로필_주소_유효성() {
        Assertions.assertThatThrownBy(() -> new Profile("qwer123!"), "프로필 패턴이 일치하지 않습니다. : " + "qwer123!");
        Assertions.assertThatThrownBy(() -> new Profile("qweasdqweasdqweasd"), "프로필 주소는 최대 15자리를 넘을 수 없습니다. : " + "qweasdqweasdqweasd");
    }
}
