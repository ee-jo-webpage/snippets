package kr.or.kosa.snippets.user.service;

import jakarta.servlet.http.HttpServletRequest;
import kr.or.kosa.snippets.user.mapper.UserMapper;
import kr.or.kosa.snippets.user.model.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * OAuth2 로그인 사용자의 정보를 받아와
 * - 신규 사용자 등록
 * - 탈퇴 사용자 복구
 * - 세션에 사용자 정보 저장
 * 등의 처리를 담당하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    /**
     * OAuth2 로그인 사용자 정보 처리
     * - 이미 존재하는 유저: 로그인
     * - 탈퇴 상태 유저: 자동 복구
     * - 존재하지 않는 유저: 자동 회원가입 처리
     * - 세션에 사용자 정보 저장 (CustomUserDetails)
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 기본 소셜 유저 정보 조회
        OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        // 현재 요청 객체를 가져와 세션에 access token 저장
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        request.getSession().setAttribute("oauth2AccessToken", userRequest.getAccessToken().getTokenValue());

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        Users user = userMapper.findByEmail(email);

        //  탈퇴한 유저라면 자동 복구 처리
        if (user != null && user.getDeletedAt() != null) {
            user.setEnabled(true);
            user.setDeletedAt(null);
            user.setReason(null);
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateUser(user);
            log.warn("탈퇴 상태의 소셜 계정이 재로그인됨. 자동 복구 처리됨: {}", email);

        }
        // 신규 유저인 경우 가입 처리
        if (user == null) {
            user = Users.builder()
                    .email(email)
                    .nickname(name)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString())) // 임의 값
                    .enabled(true)
                    .role("ROLE_OAUTH2")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .loginType("GOOGLE")
                    .build();
            userMapper.insertUser(user);
        }

        // 소셜 사용자도 CustomUserDetails 로 세션 저장
        return new CustomUserDetails(
                user.getUserId(),
                user.getEmail(),
                user.getPassword(),
                user.getNickname(),
                user.isEnabled(),
                user.getLoginType(),
                List.of(new SimpleGrantedAuthority(user.getRole())),
                oauth2User.getAttributes() //  OAuth2User의 정보
        );
    }

    /**
     * 소셜 로그인 사용자 탈퇴 처리
     * - DB 상에서 비활성화 및 삭제일자 저장
     * - Google 사용자일 경우 연결 해제 API 호출
     */
    @Transactional
    public void deleteUser(String email, String accessToken) {
        Users user = userMapper.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        //  구글 사용자라면 연결 해제
        if ("GOOGLE".equalsIgnoreCase(user.getLoginType())) {
            userService.revokeGoogleAccessToken(accessToken);
        }
        user.setDeletedAt(LocalDateTime.now());
        user.setEnabled(false);
        userMapper.updateUser(user);

        log.info("🗑 사용자 탈퇴 처리 완료: {}", email);
    }

}