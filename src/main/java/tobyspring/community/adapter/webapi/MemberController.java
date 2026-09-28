package tobyspring.community.adapter.webapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import tobyspring.community.application.provided.MemberFinder;
import tobyspring.community.application.provided.MemberRegister;
import tobyspring.community.domain.member.Member;
import tobyspring.community.domain.member.MemberDetailRequest;
import tobyspring.community.domain.member.MemberFixture;
import tobyspring.community.domain.member.RegisterMemberRequest;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberRegister memberRegister;
    private final MemberFinder memberFinder;

    @PostMapping("/member/register")
    public MemberDto registerMember(
            @RequestBody @Valid RegisterMemberRequest registerMemberRequest) {
        Assert.isTrue(registerMemberRequest != null, "registerMemberRequest가 비었습니다.");

        RegisterMemberRequest registerRequest = MemberFixture.getRegisterRequest(
                registerMemberRequest.email(),
                registerMemberRequest.nickname(),
                registerMemberRequest.password());
        Member member = memberRegister.register(registerRequest);

        return MemberDto.mapper(member);
    }

    @GetMapping("/activate/{id}")
    public MemberDto activate(@PathVariable Long id) {
        Assert.isTrue(id != null, "id가 비었습니다.");
        Member activated = memberRegister.activate(id);;

        return MemberDto.mapper(activated);
    }

    @GetMapping("/deactivate/{id}")
    public MemberDto deactivate(@PathVariable Long id) {
        Assert.isTrue(id != null, "id가 비었습니다.");
        Member deactivated = memberRegister.deactivate(id);

        return MemberDto.mapper(deactivated);
    }

    @PutMapping("/update/{id}")
    public MemberDto updateMember(@PathVariable Long id,
                                  @RequestParam @Valid String nickname,
                                  @RequestBody @Valid MemberDetailRequest detailRequest) {
        Assert.isTrue(id != null, "id가 비었습니다.");
        Member updatedMember = memberRegister.updateInfo(id, nickname, detailRequest);

        return MemberDto.mapper(updatedMember);
    }

    @GetMapping("/member/{id}")
    public MemberDto getMember(@PathVariable Long id) {
        Member memberById = memberFinder.findById(id);

        return MemberDto.mapper(memberById);
    }
}
