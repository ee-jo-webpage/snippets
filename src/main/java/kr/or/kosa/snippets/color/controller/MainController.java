package kr.or.kosa.snippets.color.controller;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


/**
 * 메인 페이지 및 사용자 전환을 위한 컨트롤러
 */
@Controller
@Slf4j
public class MainController {

    /**
     * 메인 페이지 (인덱스)
     * URL: / 또는 /index
     */
    @GetMapping(value = {"/temp", "/index"})
    public String index() {
        return "index";  // /WEB-INF/views/index.jsp
    }

    /**
     * 테스트용 사용자 전환
     * URL: /main/switch-user/{userId}
     * @param userId 전환할 사용자 ID
     * @param session HTTP 세션
     * @return 인덱스 페이지로 리다이렉트
     */
    @GetMapping("/main/switch-user/{userId}")
    public String switchUser(@PathVariable Long userId, HttpSession session) {
        session.setAttribute("userId", userId);
        return "redirect:/index";
    }

    /**
     * 폴더 관리 페이지 (임시)
     * URL: /folders
     */
    @GetMapping("/folders")
    public String folders() {
        return "folders/folder-manager";  // 폴더 관리 페이지
    }

    /**
     * 스니펫 태그 관리 페이지
     * URL: /snippet-tag-management
     */
    @GetMapping("/snippet-tag-management")
    public String showSnippetTagManagement() {
        return "tags/snippet-tag-management";  // /WEB-INF/views/tags/snippet-tag-management.jsp
    }

    /**
     * 태그 관리 페이지
     * URL: /tag-manager
     */
    @GetMapping("/tag-manager")
    public String showTagManager() {
        return "tags/tag-manager";  // /WEB-INF/views/tags/tag-manager.jsp
    }

    /**
     * 스니펫 뷰어 페이지
     * URL: /snippet-viewer
     */
    @GetMapping("/snippet-viewer")
    public String showSnippetViewer() {
        return "snippets/snippet-viewer";  // /WEB-INF/views/snippets/snippet-viewer.jsp
    }
}