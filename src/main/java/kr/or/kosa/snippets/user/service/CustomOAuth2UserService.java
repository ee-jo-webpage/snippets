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
import org.springframework.web.client.RestTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(userRequest);
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

        log.info("🗑️ 사용자 탈퇴 처리 완료: {}", email);
    }

}