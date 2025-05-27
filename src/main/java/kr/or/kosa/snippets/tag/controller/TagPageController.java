package kr.or.kosa.snippets.tag.controller;

import kr.or.kosa.snippets.basic.model.Snippets;
import kr.or.kosa.snippets.tag.model.TagItem;
import kr.or.kosa.snippets.tag.service.TagService;
import kr.or.kosa.snippets.user.service.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/tags")
@Slf4j
public class TagPageController {

    @Autowired
    private TagService tagService;

    private Long requireLogin(CustomUserDetails userDetails) {
        if (userDetails == null) {
            log.warn("사용자 인증 정보가 없습니다.");
            return null;
        }
        return userDetails.getUserId();
    }

    /**
     * 태그 관리 페이지로 이동
     * URL: /tags/manager
     */
    @GetMapping("/manager")
    public String showTagManager(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long userId = requireLogin(userDetails);
        log.info("태그 관리 페이지 접근 - 사용자 ID: {}", userId);

        // HTML에서 사용하는 변수명에 맞춰 설정
        model.addAttribute("currentUserId", userId);  // 기존 변수
        model.addAttribute("userId", userId);         // HTML에서 체크하는 변수 추가

        return "tag/temp-tag-manager";
    }

    /**
     * 스니펫 태그 관리 페이지로 이동
     * URL: /tags/snippet-tag
     */
    @GetMapping("/snippet-tag")
    public String showSnippetTagManagement(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        try {
            Long currentId = requireLogin(userDetails);
            log.info("스니펫 태그 관리 페이지 접근 - 사용자 ID: {}", currentId);

            if (currentId == null) {
                // 로그인되지 않은 경우 빈 데이터로 설정
                model.addAttribute("snippetList", new ArrayList<>());
                model.addAttribute("tags", new ArrayList<>());
                model.addAttribute("userId", null);  // 중요: HTML에서 체크하는 변수
                model.addAttribute("error", "로그인이 필요합니다.");
                return "tag/snippet-tags";
            }

            // 내가 작성한 스니펫 조회
            List<Snippets> mySnippets = tagService.getSnippetsByUserId(currentId);
            log.info("사용자 {}가 작성한 스니펫 수: {}", currentId, mySnippets != null ? mySnippets.size() : 0);

            // 내가 만든 태그 목록 조회
            List<TagItem> myTags = tagService.getTagsByUserId(currentId);
            log.info("사용자 {}가 만든 태그 수: {}", currentId, myTags != null ? myTags.size() : 0);

            // null 체크 및 기본값 설정
            model.addAttribute("snippetList", mySnippets != null ? mySnippets : new ArrayList<>());
            model.addAttribute("tags", myTags != null ? myTags : new ArrayList<>());
            model.addAttribute("userId", currentId);  // 중요: HTML에서 체크하는 변수

        } catch (Exception e) {
            log.error("스니펫/태그 조회 중 오류 발생", e);
            model.addAttribute("snippetList", new ArrayList<>());
            model.addAttribute("tags", new ArrayList<>());
            model.addAttribute("userId", null);  // 중요: HTML에서 체크하는 변수
            model.addAttribute("error", "데이터를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
        }

        return "tag/snippet-tags";
    }
}