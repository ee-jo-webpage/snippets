package kr.or.kosa.snippets.color.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kr.or.kosa.snippets.color.model.Color;
import kr.or.kosa.snippets.color.service.ColorService;
import kr.or.kosa.snippets.user.service.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/color")
public class ColorController {

    @Autowired
    private ColorService colorService;

    // private 헬퍼 메서드
    private Long requireLogin(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다");
        }
        return userDetails.getUserId();
    }

    @GetMapping("/check-session")
    @ResponseBody
    public Map<String, Object> checkSession(HttpServletRequest request) {
        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("userId", request.getSession().getAttribute("userId"));
        sessionData.put("sessionId", request.getSession().getId());
        sessionData.put("hasUserId", request.getSession().getAttribute("userId") != null);
        return sessionData;
    }

    // 현재 사용자 정보 확인 엔드포인트
    @GetMapping("/session-info")
    @ResponseBody
    public Map<String, Object> getSessionInfo(HttpSession session) {
        Map<String, Object> info = new HashMap<>();
        info.put("userId", session.getAttribute("userId"));
        info.put("sessionId", session.getId());
        return info;
    }

    //등록된 전체 색상 조회
    @GetMapping("/all-colors")
    public String getColors(Model model) {
        log.info("전체 색상 조회");

        List<Color> colorList = colorService.getAllColors();
        model.addAttribute("colorList", colorList);

        return "color/color-list";
    }

    // 사용자가 사용 가능한 모든 색상 조회 (기본 색상 + 사용자 지정 색상)
    @GetMapping("/user/{userId}")
    public String getAvailableColors(@PathVariable Long userId,
                                     HttpSession session,
                                     Model model) {
        log.info("사용자 {}의 사용 가능한 색상 조회", userId);
        session.setAttribute("userId", userId);

        try {
            List<Color> colorList = colorService.getAllAvailableColorsByUserId(userId);

            model.addAttribute("colorList", colorList);
            model.addAttribute("userId", userId);
            model.addAttribute("currentUserId", session.getAttribute("userId"));
            model.addAttribute("isMyColors", false);
            model.addAttribute("isAvailableColors", true);

            return "color/user-colors";
        } catch (Exception e) {
            log.error("사용자 {}의 사용 가능한 색상 조회 중 오류: {}", userId, e.getMessage(), e);
            model.addAttribute("error", "색상 정보를 불러오는 중 오류가 발생했습니다.");
            model.addAttribute("colorList", List.of());
            model.addAttribute("isMyColors", false);  // null 방지
            return "color/user-colors";
        }
    }

    // 로그인한 사용자 자신의 색상만 조회하는 경우
    @GetMapping("/my-colors")
    public String getMyColors(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long userId = requireLogin(userDetails);
        // Long userId = userDetails.getUserId(); // 시큐리티 체인 수정시 얘만 남길것 requireLogin 삭제

        List<Color> colors = colorService.getAllAvailableColorsByUserId(userId);

        model.addAttribute("colorList", colors);
        model.addAttribute("userId", userId);
        model.addAttribute("currentUserId", userId);
        model.addAttribute("isMyColors", true);
        model.addAttribute("isAvailableColors", true);

        return "color/user-colors";
    }

    //색상등록
    @PostMapping("/add")
    public String createColor(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @ModelAttribute Color color,
                              RedirectAttributes redirectAttrs) {
        Long userId = null;
        try {
            userId = requireLogin(userDetails);
            // Long userId = userDetails.getUserId(); // 시큐리티 체인 수정시 얘만 남길것 requireLogin 삭제

            color.setUserId(userId);

            log.info("색상 등록 시도 - 사용자 ID: {}, 색상명: {}", userId, color.getName());

            colorService.insertColor(color);

            redirectAttrs.addFlashAttribute("message", "색상이 성공적으로 추가되었습니다.");
            redirectAttrs.addFlashAttribute("messageType", "success");

            return "redirect:/color/my-colors";

        } catch (ResponseStatusException e) {
            log.error("인증 실패", e);
            return "redirect:/login";
        } catch (Exception e) {
            log.error("색상 등록 실패", e);
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            redirectAttrs.addFlashAttribute("messageType", "error");

            if (userId != null) {
                return "redirect:/color/my-colors";
            } else {
                return "redirect:/login";
            }
        }
    }

    //색상수정
    @PostMapping("/update")
    public String updateColor(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @ModelAttribute Color color,
                              RedirectAttributes redirectAttrs) {
        try {
            Long userId = requireLogin(userDetails);
            // Long userId = userDetails.getUserId(); // 시큐리티 체인 수정시 얘만 남길것 requireLogin 삭제

            color.setUserId(userId);

            colorService.updateColor(color);

            redirectAttrs.addFlashAttribute("message", "색상이 수정되었습니다.");

            return "redirect:/color/my-colors";
        } catch (ResponseStatusException e) {
            log.error("인증 실패", e);
            return "redirect:/login";
        } catch (Exception e) {
            log.error("색상 수정 실패", e);
            redirectAttrs.addFlashAttribute("error", e.getMessage());

            return "redirect:/color/my-colors";
        }
    }

    //색상삭제
    @PostMapping("/delete")
    public String deleteColor(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @RequestParam Long colorId,
                              RedirectAttributes redirectAttrs) {
        try {
            Long userId = requireLogin(userDetails);
            // Long userId = userDetails.getUserId(); // 시큐리티 체인 수정시 얘만 남길것 requireLogin 삭제

            colorService.deleteColor(colorId, userId);

            redirectAttrs.addFlashAttribute("message", "색상이 성공적으로 삭제되었습니다.");

            return "redirect:/color/my-colors";
        } catch (ResponseStatusException e) {
            log.error("인증 실패", e);
            return "redirect:/login";
        } catch (Exception e) {
            log.error("색상 삭제 실패", e);
            redirectAttrs.addFlashAttribute("error", e.getMessage());

            return "redirect:/color/my-colors";
        }
    }
}