package tobyspring.community.domain;

import org.jspecify.annotations.NonNull;
import tobyspring.community.application.provided.Email;

public class MemberFixture {
    public static @NonNull RegisterMemberRequest getRegisterRequest() {
        return getRegisterRequest("qwer@naver.com");
    }

    public static @NonNull RegisterMemberRequest getRegisterRequest(String email) {
        return new RegisterMemberRequest(email, "abcd", "qwe123");
    }
}
