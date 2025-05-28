package kr.or.kosa.snippets.user.controller;

import kr.or.kosa.snippets.snippetExt.service.SnippetExtService;
import kr.or.kosa.snippets.user.service.CustomUserDetails;
import kr.or.kosa.snippets.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collection;

@Controller
@RequiredArgsConstructor
public class pageController {

    private final UserService userService;
    private final SnippetExtService snippetExtService;

    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal CustomUserDetails details) {
        if (details != null) {
            String email = details.getName();
            String nickname = userService.getNicknameByEmail(email);
            model.addAttribute("nickname", nickname);

            boolean isAdmin = details.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            model.addAttribute("isAdmin", isAdmin);
        }

        model.addAttribute("topSnippets", snippetExtService.getTop3PopularSnippets());
        return "main";
    }

}