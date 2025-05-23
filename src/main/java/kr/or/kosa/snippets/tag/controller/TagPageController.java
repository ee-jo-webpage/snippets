package kr.or.kosa.snippets.tag.controller;

import kr.or.kosa.snippets.basic.model.Snippets;
import kr.or.kosa.snippets.basic.service.SnippetService;
import kr.or.kosa.snippets.tag.service.TagService;
import kr.or.kosa.snippets.user.service.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
@RequestMapping("/tags")
@Slf4j
public class TagPageController {

    private final SnippetService snippetService;
    private final TagService tagService;

    public TagPageController(SnippetService snippetService, TagService tagService) {
        this.snippetService = snippetService;
        this.tagService = tagService;
    }

    private Long requireLogin(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다");
        }
        return userDetails.getUserId();
    }

    /**
     * 태그 관리 페이지로 이동
     * URL: /tags/manager
     */
    @GetMapping("/manager")
    public String showTagManager(@AuthenticationPrincipal CustomUserDetails userDetails) {

        Long currentId = requireLogin(userDetails);
//        Long currentId = userDetails.getUserId();

        return "tag/tag-manager";  // /WEB-INF/views/tags/tag-manager.jsp로 이동
    }

    /**
     * 스니펫 태그 관리 페이지로 이동
     * URL: /tags/snippet-management
     */
    @GetMapping("/snippet-tag")
    public String showSnippetTagManagement(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        //Long currentId = requireLogin(userDetails);
        Long currentId = userDetails.getUserId();

        List<Snippets> snippetList = tagService.getSnippetsByUserId(currentId);
        System.out.println("---------------------------------------" + snippetList );
        model.addAttribute("userId", currentId);
        model.addAttribute("currentId", currentId);
        model.addAttribute("snippetList", snippetList);

        return "tag/my-snippet";
    }
}