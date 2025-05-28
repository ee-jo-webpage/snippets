package kr.or.kosa.snippets.app.controller;

import jakarta.servlet.http.HttpSession;
import kr.or.kosa.snippets.bookmark.service.BookmarkService;
import kr.or.kosa.snippets.basic.service.SnippetService;
import kr.or.kosa.snippets.tag.service.TagService;
import kr.or.kosa.snippets.user.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/app")
@RequiredArgsConstructor
public class MainLayoutController {

    private final SnippetService snippetService;
    private final BookmarkService bookmarkService;
    private final TagService tagService;

    /**
     * 메인 대시보드 페이지
     */
    @GetMapping
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails,
                            HttpSession session,
                            Model model) {
        log.info("메인 대시보드 접근");

        // 페이지 기본 정보 설정
        model.addAttribute("pageTitle", "대시보드");
        model.addAttribute("pageSubtitle", "코드 스니펫 관리의 모든 것을 한눈에 확인하세요");
        model.addAttribute("showStats", true);
        model.addAttribute("showActions", true);

        // 사용자 정보가 있는 경우 통계 데이터 로드
        if (userDetails != null) {
            Long userId = userDetails.getUserId();
//            loadUserStats(userId, model);
//            loadSidebarCounts(userId, model);
        } else {
            // 로그인하지 않은 경우 기본 통계
//            loadDefaultStats(model);
        }

        return "fragments/layout";
    }
}