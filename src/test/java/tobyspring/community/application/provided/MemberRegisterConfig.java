package tobyspring.community.application.provided;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import tobyspring.community.application.required.EmailSender;
import tobyspring.community.domain.member.MemberFixture;
import tobyspring.community.domain.member.PasswordEncode;

@TestConfiguration
public class MemberRegisterConfig {

    @Bean
    public PasswordEncode passwordEncoder() {
        return MemberFixture.createPasswordEncoder();
    }

    @Bean
    public EmailSender emailSender() {
        return (email, subject, body) -> {
            System.out.println("sending email : " + email);
            return false;
        };
    }
}
