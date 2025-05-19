package kr.or.kosa.snippets.user.controller;

import kr.or.kosa.snippets.user.model.Users;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class AuthController {
    @GetMapping("/login")
    public String login(Principal principal){
        // 이미 로그인되어 있으면 홈으로
        if (principal != null) return "redirect:/";
        return "/user/auth/login";
    }
    @GetMapping("/register")
    public String showRegisterForm(Model model, Principal principal) {
        if (principal != null) return "redirect:/";
        model.addAttribute("user", new Users());
        return "/user/auth/register";
    }

}