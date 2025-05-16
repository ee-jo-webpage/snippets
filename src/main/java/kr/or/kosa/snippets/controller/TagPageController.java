package kr.or.kosa.snippets.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/tags")
public class TagPageController {

    /**
     * 태그 관리 페이지로 이동
     * URL: /tags/manager
     */
    @GetMapping("/manager")
    public String showTagManager() {
        return "tags/tag-manager";  // /WEB-INF/views/tags/tag-manager.jsp로 이동
    }

    /**
     * 스니펫 태그 관리 페이지로 이동
     * URL: /tags/snippet-management
     */
    @GetMapping("/snippet-management")
    public String showSnippetTagManagement() {
        return "tags/snippet-tag-management";
    }
}