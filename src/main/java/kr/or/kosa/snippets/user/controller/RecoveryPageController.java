package kr.or.kosa.snippets.user.controller;

import kr.or.kosa.snippets.user.service.MailService;
import kr.or.kosa.snippets.user.service.UserRecoveryService;
import kr.or.kosa.snippets.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class RecoveryPageController {

    private final MailService mailService;
    private final UserService userService;
    private final UserRecoveryService userRecoveryService;

    @GetMapping("/reactivate")
    public String reactivate(@RequestParam String email,
                             @RequestParam String token,
                             Model model) {
        if (!mailService.verifyRecoveryToken(email, token)) {
            model.addAttribute("errorMessage", "토큰이 유효하지 않거나 만료되었습니다.");
            return "/user/recovery/reactivateError";
        }

        boolean restored = userRecoveryService.restoreUser(email);
        mailService.deleteRecoveryToken(email);

        if (!restored) {
            model.addAttribute("errorMessage", "복구에 실패했습니다. 이미 복구되었을 수 있습니다.");
            return "/user/recovery/reactivateError";
        }

        return "/user/recovery/reactivate"; // 복구 성공 페이지
    }
}