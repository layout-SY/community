package tobyspring.community.domain.member;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tobyspring.community.domain.EntityAbstract;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class MemberDetail extends EntityAbstract {

    @Embedded
    private Profile profile;

    private String introduction;

    private LocalDateTime registeredAt;

    private LocalDateTime activatedAt;

    private LocalDateTime deactivatedAt;

    static MemberDetail create() {
        MemberDetail memberDetail = new MemberDetail();

        memberDetail.registeredAt = LocalDateTime.now();

        return memberDetail;
    }

    public void activate() {
        this.activatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.deactivatedAt = LocalDateTime.now();
    }

    public void updateInfo(MemberDetailRequest detailRequest) {
        this.profile = new Profile(detailRequest.profile());
        this.introduction = detailRequest.introduction();
    }
}
