package kr.or.kosa.snippets.user.controller;

import kr.or.kosa.snippets.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class pageController {
    private final UserService userService;
    @GetMapping("/")
    public String index(Model model, Principal principal) {
        if (principal != null) {
            String email = principal.getName();
            String nickname = userService.getNicknameByEmail(email);
            model.addAttribute("nickname", nickname);
        }
        return "index";
    }
}