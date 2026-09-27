package tobyspring.community.domain.member;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.springframework.util.Assert;
import tobyspring.community.domain.EntityAbstract;
import tobyspring.community.domain.shared.Email;

@NoArgsConstructor
@Getter
@NaturalIdCache
@ToString
@Entity
@Table
public class Member extends EntityAbstract {
    @NaturalId
    @Embedded
    @Column(nullable=false)
    private Email email;

    @Column(length = 150)
    private String nickname;

    @Column(length = 200)
    private String passwordHash;

    @Column(length = 100)
    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    @OneToOne(cascade = CascadeType.ALL)
    private MemberDetail detail;

    public static Member register(RegisterMemberRequest registerRequest, PasswordEncode passwordEncoder) {
        Member member = new Member();

        member.email = new Email(registerRequest.email());
        member.nickname = registerRequest.nickname();
        member.passwordHash = passwordEncoder.encode(registerRequest.password());
        member.status = MemberStatus.PENDING;

        member.detail = MemberDetail.create();

        return member;
    }

    public void activate() {
        this.status = MemberStatus.ACTIVE;
        this.detail.activate();
    }

    public void deactivate() {
        Assert.state(this.status == MemberStatus.ACTIVE, "Member is not active");
        this.status = MemberStatus.DEACTIVATED;
        this.detail.deactivate();
    }

    public boolean verifyPassword(String password, PasswordEncode passwordEncoder) {
        return passwordEncoder.match(password, passwordHash);
    }

    public void changePassword(String newPassword, PasswordEncode passwordEncoder) {
        Assert.state(this.status == MemberStatus.ACTIVE, "Member is not active");
        this.passwordHash = passwordEncoder.encode(newPassword);
    }

    public Member updateInfo(String nickname, MemberDetailRequest detailRequest) {
        Assert.state(this.status == MemberStatus.ACTIVE, "Member is not active");

        this.nickname = nickname;
        this.detail.updateInfo(detailRequest);

        return this;
    }
}
