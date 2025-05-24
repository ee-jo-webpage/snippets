package kr.or.kosa.snippets.navigation.controller;

import kr.or.kosa.snippets.user.service.CustomUserDetails;
import kr.or.kosa.snippets.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class NavigationController {

    private final UserService userService;

    @GetMapping("/navigation")
    public String showDevNavigation(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    Principal principal,
                                    HttpSession session,
                                    Model model) {

        // Spring Security 로그인 정보 확인
        if (userDetails != null) {
            log.info("🔐 Spring Security 인증 사용자: {}", userDetails.getEmail());
            model.addAttribute("securityUser", userDetails);
            model.addAttribute("securityUserId", userDetails.getUserId());
            model.addAttribute("securityEmail", userDetails.getEmail());
            model.addAttribute("securityNickname", userDetails.getNickname());
            model.addAttribute("loginType", userDetails.getLoginType());

            // 세션에 Spring Security userId 저장 (기존 시스템과 호환성 유지)
            session.setAttribute("userId", userDetails.getUserId());
        } else if (principal != null) {
            // OAuth2 로그인 등 다른 방식의 인증
            String email = principal.getName();
            String nickname = userService.getNicknameByEmail(email);
            model.addAttribute("principalEmail", email);
            model.addAttribute("principalNickname", nickname);
        }

        // 기존 세션 userId도 함께 확인 (레거시 호환)
        Long sessionUserId = (Long) session.getAttribute("userId");
        if (sessionUserId != null) {
            log.info("📌 세션 userId: {}", sessionUserId);
            model.addAttribute("sessionUserId", sessionUserId);
        }

        // 세션 정보
        model.addAttribute("sessionId", session.getId());

        return "navigation";
    }
}