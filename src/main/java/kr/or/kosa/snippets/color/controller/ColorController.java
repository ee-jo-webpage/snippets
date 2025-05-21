package kr.or.kosa.snippets.color.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kr.or.kosa.snippets.color.model.Color;
import kr.or.kosa.snippets.color.service.ColorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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

    // 개선된 세션 처리 메서드
    private Long getUserIdFromSession(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        // 세션에 userId가 없으면 자동으로 생성
        if (userId == null) {
            userId = 1L;  // 기본 사용자 ID
            session.setAttribute("userId", userId);
            log.info("세션에 기본 사용자 ID 설정: {}", userId);
        }

        return userId;
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

    //사용자별 색상 조회
    @GetMapping("/user/{userId}")
    public String userColors(@PathVariable Long userId,
                             HttpSession session,
                             Model model) {

        log.info(userId + "의 색상 조회");
        session.setAttribute("userId", userId);

        // 세션 설정 직후 확인
        log.info("세션에 저장된 userId: {}", session.getAttribute("userId"));
        log.info("세션 ID: {}", session.getId());

        List<Color> colorList = colorService.getColorsByUserId(userId);

        model.addAttribute("colorList", colorList);
        model.addAttribute("userId", userId);
        model.addAttribute("currentUserId", session.getAttribute("userId")); // 추가
        model.addAttribute("isMyColors", false); // 추가

        return "color/user-colors";
    }

    // 로그인한 사용자 자신의 색상만 조회하는 경우(세션)
//    @GetMapping("/my-colors")
//    public String getMyColors(Model model, HttpSession session) {
//        // 세션에서 사용자 ID 가져오기 (실제로는 Spring Security 사용)
//        Long currentUserId = (Long) session.getAttribute("userId");
//
//        if (currentUserId == null) {
//            return "redirect:/login";  // 로그인되지 않은 경우
//        }
//
//        log.info("현재 사용자 {}의 색상 조회", currentUserId);
//        List<Color> colorList = colorService.getColorsByUserId(currentUserId);
//
//        model.addAttribute("colorList", colorList);
//        model.addAttribute("isMyColors", true);
//        return "colors/user-colors";
//    }


    //색상등록
    //색상등록
    @PostMapping("/add")
    public String createColor(@ModelAttribute Color color, HttpSession session, RedirectAttributes redirectAttrs){
        try {
            // 세션에서 사용자 ID 가져오기
            Long userId = getUserIdFromSession(session);
            color.setUserId(userId);

            log.info("색상 등록 시도 - 사용자 ID: {}, 색상명: {}", userId, color.getName());

            colorService.insertColor(color);

            redirectAttrs.addFlashAttribute("message", "색상이 성공적으로 추가되었습니다.");
            redirectAttrs.addFlashAttribute("messageType", "success");

            // 사용자 색상 페이지로 리다이렉트
            return "redirect:/color/user/" + userId;

        } catch (Exception e) {
            log.error("색상 등록 실패", e);
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            redirectAttrs.addFlashAttribute("messageType", "error");

            // 에러 시에도 사용자 페이지로
            Long userId = getUserIdFromSession(session);
            return "redirect:/color/user/" + userId;
        }
    }

    //색상수정
    @PostMapping("/update")
    public String updateColor(@ModelAttribute Color color,
                              HttpSession session,
                              RedirectAttributes redirectAttrs) {
        try {
            Long userId = getUserIdFromSession(session);
            color.setUserId(userId);

            colorService.updateColor(color);

            redirectAttrs.addFlashAttribute("message", "색상이 수정되었습니다.");

            return "redirect:/color/user/" + userId;
        } catch (Exception e) {
            log.error("색상 수정 실패", e);
            redirectAttrs.addFlashAttribute("error", e.getMessage());

            Long userId = getUserIdFromSession(session);
            return "redirect:/color/user/" + userId;
        }
    }

    //색상삭제
    @PostMapping("/delete")
    public String deleteColor(@RequestParam Long colorId,
                              HttpSession session,
                              RedirectAttributes redirectAttrs) {
        try {
            Long userId = getUserIdFromSession(session);

            colorService.deleteColor(colorId, userId);

            redirectAttrs.addFlashAttribute("message", "색상이 성공적으로 삭제되었습니다.");

            return "redirect:/color/user/" + userId;
        } catch (Exception e) {
            log.error("색상 삭제 실패", e);
            redirectAttrs.addFlashAttribute("error", e.getMessage());

            Long userId = getUserIdFromSession(session);
            return "redirect:/color/user/" + userId;
        }
    }
}