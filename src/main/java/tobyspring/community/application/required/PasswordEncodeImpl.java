package tobyspring.community.application.required;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncodeImpl implements PasswordEncode{
    public String encode(String password) {
        return password.toUpperCase();
    }

    public boolean match(String password, String encodedPassword) {
        return encode(password).equals(encodedPassword);
    }
}
