package gift.auth;

import gift.exception.UnauthorizedException;
import gift.member.Member;
import gift.member.MemberService;
import org.springframework.stereotype.Component;

/**
 * Resolves the authenticated member from an Authorization header.
 *
 * @author brian.kim
 * @since 1.0
 */
@Component
public class AuthenticationResolver {
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;
    private final MemberService memberService;

    public AuthenticationResolver(JwtProvider jwtProvider, MemberService memberService) {
        this.jwtProvider = jwtProvider;
        this.memberService = memberService;
    }

    public Member extractMember(String authorization) {
        try {
            String token = authorization.replace(BEARER_PREFIX, "");
            String email = jwtProvider.getEmail(token);
            return memberService.getMemberByEmail(email);
        } catch (Exception e) {
            throw new UnauthorizedException("유효하지 않은 인증 정보입니다.");
        }
    }
}
