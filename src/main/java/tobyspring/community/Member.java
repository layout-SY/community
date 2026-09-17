package tobyspring.community;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@NoArgsConstructor
@Getter
public class Member {

    private Email email;

    private String nickname;

    private String passwordHash;

    private MemberStatus status;

    public static Member register(RegisterMemberRequest registerRequest, PasswordEncoder passwordEncoder) {
        Member member = new Member();

        member.email = registerRequest.email();
        member.nickname = registerRequest.nickname();
        member.passwordHash = passwordEncoder.encode(registerRequest.password());
        member.status = MemberStatus.PENDING;

        return member;
    }

    public void activate() {
        this.status = MemberStatus.ACTIVE;
    }

    public void deactivate() {
        Assert.state(this.status == MemberStatus.ACTIVE, "Member is not active");
        this.status = MemberStatus.DEACTIVATED;
    }

    public boolean verifyPassword(String password, PasswordEncoder passwordEncoder) {
        return passwordEncoder.match(password, passwordHash);
    }

    public void changePassword(String newPassword, PasswordEncoder passwordEncoder) {
        Assert.state(this.status == MemberStatus.ACTIVE, "Member is not active");
        this.passwordHash = passwordEncoder.encode(newPassword);
    }
}
