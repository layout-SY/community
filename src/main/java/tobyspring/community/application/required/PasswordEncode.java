package tobyspring.community.application.required;

public interface PasswordEncode {
    String encode(String password);

    boolean match(String password, String encodedPassword);
}
