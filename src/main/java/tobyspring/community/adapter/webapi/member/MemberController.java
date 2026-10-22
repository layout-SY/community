package tobyspring.community.adapter.webapi.member;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import tobyspring.community.adapter.security.TokenExtractor;
import tobyspring.community.adapter.security.TokenManager;
import tobyspring.community.application.member.MemberAuth;
import tobyspring.community.application.member.provided.MemberFinder;
import tobyspring.community.application.member.provided.MemberRegister;
import tobyspring.community.domain.member.*;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberRegister memberRegister;
    private final MemberFinder memberFinder;
    private final MemberAuth memberAuth;
    private final TokenManager tokenManager;

    @PostMapping("/member/register")
    public MemberDto registerMember(
            @RequestBody @Valid RegisterMemberRequest registerMemberRequest) {
        Assert.notNull(registerMemberRequest.email(), "email이 비었습니다.");

        RegisterMemberRequest registerRequest = MemberFixture.getRegisterRequest(
                registerMemberRequest.email(),
                registerMemberRequest.nickname(),
                registerMemberRequest.password());
        Member member = memberRegister.register(registerRequest);

        return MemberDto.mapper(member);
    }

    @GetMapping("/activate")
    public MemberDto activate(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION)
            String token) {
        String extractedToken = TokenExtractor.extractToken(token);
        Long memberId = tokenManager.extractMemberIdFromToken(extractedToken);

        Member activated = memberRegister.activate(memberId);

        return MemberDto.mapper(activated);
    }

    @GetMapping("/deactivate")
    public MemberDto deactivate(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION)
            String token) {
        String extractedToken = TokenExtractor.extractToken(token);
        Long memberId = tokenManager.extractMemberIdFromToken(extractedToken);

        Member deactivated = memberRegister.deactivate(memberId);

        return MemberDto.mapper(deactivated);
    }

    @PutMapping("/update")
    public MemberDto updateMember(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION)
            String token,

            @RequestParam @Valid String nickname,
            @RequestBody @Valid MemberDetailRequest detailRequest) {
        String extractedToken = TokenExtractor.extractToken(token);
        Long memberId = tokenManager.extractMemberIdFromToken(extractedToken);

        Member updatedMember = memberRegister.updateInfo(memberId, nickname, detailRequest);

        return MemberDto.mapper(updatedMember);
    }

    @GetMapping("/member/{id}")
    public MemberDto getMember(@PathVariable Long id) {
        Member memberById = memberFinder.findById(id);

        return MemberDto.mapper(memberById);
    }

    @PostMapping("/member/login")
    public LoginResponseDto login(@RequestBody LoginRequest request) {
        return memberAuth.login(request.email(), request.password());
    }
}
