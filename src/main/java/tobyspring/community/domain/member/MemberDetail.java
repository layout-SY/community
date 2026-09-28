package tobyspring.community.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;
import tobyspring.community.domain.EntityAbstract;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class MemberDetail extends EntityAbstract {

    @Embedded
    private Profile profile;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    @Column(nullable = false)
    private LocalDateTime registeredAt;

    private LocalDateTime activatedAt;

    private LocalDateTime deactivatedAt;

    static MemberDetail create() {
        MemberDetail memberDetail = new MemberDetail();

        memberDetail.registeredAt = LocalDateTime.now();

        return memberDetail;
    }

    public void activate() {
        Assert.isTrue(activatedAt == null, "이미 activatedAt은 설정되어 있습니다.");
        this.activatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        Assert.isTrue(deactivatedAt == null, "이미 deactivatedAt은 설정되어 있습니다.");
        this.deactivatedAt = LocalDateTime.now();
    }

    public void updateInfo(MemberDetailRequest detailRequest) {
        this.profile = new Profile(detailRequest.profile());
        this.introduction = detailRequest.introduction();
    }
}
