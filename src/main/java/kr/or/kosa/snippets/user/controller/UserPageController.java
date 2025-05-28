package kr.or.kosa.snippets.user.controller;

import kr.or.kosa.snippets.user.model.Users;
import kr.or.kosa.snippets.user.service.CustomUserDetails;
import kr.or.kosa.snippets.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
@Slf4j
@Controller
@RequiredArgsConstructor
public class UserPageController {
    private final UserService userService;

    @GetMapping("/user/mypage")
    public String myPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long userId = userDetails.getUserId();  // 세션에서 userId 직접 꺼냄
        model.addAttribute("email", userDetails.getEmail());
        model.addAttribute("nickname", userDetails.getNickname());
        return "/user/user/myPage";
    }


    @GetMapping("/forgotPassword")
    public String forgotPassword(Model model) {
        return "/user/user/forgotPassword";
    }

    @GetMapping("/changePassword")
    public String changePasswordPage(Principal principal) {
        if (principal == null) return "redirect:/login";
        return "/user/user/changePassword";
    }

}