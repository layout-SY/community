package tobyspring.community.domain.member;

public interface PasswordEncode {
    String encode(String password);

    boolean match(String password, String encodedPassword);
}
