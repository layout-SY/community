package tobyspring.community;

public class PasswordEncoder {
    public String encode(String password) {
        return password.toUpperCase();
    }

    public boolean match(String password, String encodedPassword) {
        return encode(password).equals(encodedPassword);
    }
}
