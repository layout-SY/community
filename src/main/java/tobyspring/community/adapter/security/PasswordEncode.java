package tobyspring.community.adapter.security;

public interface PasswordEncode {
    String encode(String password);

    boolean match(String password, String encodedPassword);
}
