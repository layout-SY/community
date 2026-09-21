package tobyspring.community.domain;

import org.springframework.context.annotation.Fallback;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Fallback
public class PasswordEncodeImpl implements PasswordEncode {
    BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    public String encode(String password) {
        return bCryptPasswordEncoder.encode(password);
    }

    public boolean match(String password, String encodedPassword) {
        return bCryptPasswordEncoder.matches(password, encodedPassword);
    }
}
