package tobyspring.community.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

import static org.springframework.util.Assert.state;

@NoArgsConstructor
@Getter
@Embeddable
public class Email {
    private String address;

    private final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

    public Email(String address) {
        state(EMAIL_PATTERN.matcher(address).matches(), "Invalid email address : " + address);
        this.address = address;
    }
}
