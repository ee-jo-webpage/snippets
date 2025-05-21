package kr.or.kosa.snippets.tag.controller;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Slf4j
@RequestMapping("/tag")
public class TagSessionController {
    @GetMapping("/init-session")
    public String initSession(HttpSession session, RedirectAttributes redirectAttributes) {
        //임시 사용자 ID를 세션에 저장
        session.setAttribute("userId", 1L);

        log.info("세션 초기화 - 임시사용자 ID: 1");

        redirectAttributes.addFlashAttribute("message", "임시사용자로 로그인되었습니다");

        return "redirect:/tags/manager";
    }

    @GetMapping("/switch-user/{userId}")
    public String switchUser(@PathVariable Long userId,
                             HttpSession session,
                             RedirectAttributes redirectAttrs) {
        session.setAttribute("userId", userId);

        log.info("사용자 전환 - 새 사용자 ID: {}", userId);
        redirectAttrs.addFlashAttribute("message",
                "사용자 " + userId + "로 전환되었습니다.");

        return "redirect:/color/all-colors";
    }


}
