package kr.or.kosa.snippets.user.controller;

import kr.or.kosa.snippets.user.model.Users;
import kr.or.kosa.snippets.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class UserPageController {
    private final UserService userService;

    @GetMapping("/user/mypage")
    public String myPage(Model model, Principal principal) {
        String email = principal.getName();
        Users user = userService.findByEmail(email);
        model.addAttribute("user", user);
        return "/user/user/myPage";
    }

}