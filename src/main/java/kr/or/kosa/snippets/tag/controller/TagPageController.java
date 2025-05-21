package kr.or.kosa.snippets.tag.controller;

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
        return "tag/tag-manager";  // /WEB-INF/views/tags/tag-manager.jsp로 이동
    }

}