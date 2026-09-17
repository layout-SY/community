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

    public Member register(RegisterMemberRequest registerRequest, PasswordEncoder passwordEncoder) {
        Member member = new Member();

        member.email = registerRequest.email();
        member.nickname = registerRequest.nickname();
        member.passwordHash = passwordEncoder.encode(registerRequest.password());
        member.status = MemberStatus.PENDING;

        return member;
    }

    public void activate(Member member) {
        member.status = MemberStatus.ACTIVE;
    }

    public void deactivate(Member member) {
        Assert.state(member.status == MemberStatus.ACTIVE, "Member is not active");
        member.status = MemberStatus.DEACTIVATED;
    }

    public boolean verifyPassword(String password, PasswordEncoder passwordEncoder) {
        return passwordEncoder.match(password, passwordHash);
    }

    public void changePassword(Member member, String newPassword, PasswordEncoder passwordEncoder) {
        Assert.state(member.status == MemberStatus.ACTIVE, "Member is not active");
        this.passwordHash = passwordEncoder.encode(newPassword);
    }
}
