package tobyspring.community.domain.member;

import org.jspecify.annotations.NonNull;
import tobyspring.community.adapter.security.PasswordEncode;
import tobyspring.community.adapter.security.PasswordEncodeImpl;

public class MemberFixture {
    public static @NonNull RegisterMemberRequest getRegisterRequest() {
        return getRegisterRequest("qwer123@naver.com");
    }

    public static @NonNull RegisterMemberRequest getRegisterRequest(String email) {
        return getRegisterRequest(email, "abcd", "qwe123");
    }

    public static @NonNull RegisterMemberRequest getRegisterRequest(String email, String nickname, String password) {
        return new RegisterMemberRequest(email, nickname, password);
    }

    public static @NonNull PasswordEncode createPasswordEncoder() {
        return new PasswordEncodeImpl();
    }

    public static @NonNull MemberDetailRequest getMemberDetailRequest() {
        return getMemberDetailRequest("qwerasdf");
    }

    public static @NonNull MemberDetailRequest getMemberDetailRequest(String profile) {
        return new MemberDetailRequest(profile, "자기소개");
    }
}
