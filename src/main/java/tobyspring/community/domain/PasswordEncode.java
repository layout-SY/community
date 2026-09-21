package tobyspring.community.domain;

public interface PasswordEncode {
    String encode(String password);

    boolean match(String password, String encodedPassword);
}
