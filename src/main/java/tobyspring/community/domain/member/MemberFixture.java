package tobyspring.community.domain.member;

import org.jspecify.annotations.NonNull;

public class MemberFixture {
    public static @NonNull RegisterMemberRequest getRegisterRequest() {
        return getRegisterRequest("qwer123@naver.com");
    }

    public static @NonNull RegisterMemberRequest getRegisterRequest(String email) {
        return new RegisterMemberRequest(email, "abcd", "qwe123");
    }

    public static @NonNull PasswordEncode createPasswordEncoder() {
        return new PasswordEncodeImpl();
    }
}
