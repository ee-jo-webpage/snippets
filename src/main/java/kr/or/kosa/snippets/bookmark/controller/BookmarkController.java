package kr.or.kosa.snippets.bookmark.controller;

import jakarta.servlet.http.HttpSession;
import kr.or.kosa.snippets.basic.model.Snippets;
import kr.or.kosa.snippets.bookmark.model.Bookmark;
import kr.or.kosa.snippets.bookmark.service.BookmarkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@RequestMapping("/bookmark")
@Slf4j
@Controller
public class BookmarkController {

    @Autowired
    BookmarkService bookmarkService;

    //사용자별 북마크 조회
    @GetMapping
    public String getAllBookmarksByUserId(HttpSession session, Model model) {
        Long currentId = (Long) session.getAttribute("userId");
        log.info("사용자 ID: {}", currentId);

        List<Snippets> bookmarkList = bookmarkService.getAllBookmarkByUserId(currentId);
        log.info("조회된 북마크 수: {}", bookmarkList.size());

        model.addAttribute("bookmarks", bookmarkList);
        model.addAttribute("count", bookmarkList.size());

        return "bookmark/bookmark-list";
    }

    //북마크된 스니펫 상세보기 (추가)
    @GetMapping("/snippet/{snippetId}")
    public String getBookmarkedSnippetDetail(@PathVariable Long snippetId, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");

        // 북마크된 스니펫인지 확인
        if (!bookmarkService.isBookmarked(userId, snippetId)) {
            return "redirect:/bookmark"; // 북마크되지 않은 스니펫이면 북마크 목록으로
        }

        Snippets snippet = bookmarkService.getSnippetById(snippetId);
        if (snippet == null) {
            return "error/404";
        }

        model.addAttribute("snippet", snippet);
        model.addAttribute("isBookmarked", true);
        return "bookmark/snippet-detail"; // 북마크용 상세 페이지
    }

    //북마크에서 제거 (POST 방식)
    @PostMapping("/remove/{snippetId}")
    public String removeBookmarkFromList(@PathVariable Long snippetId, HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");

        try {
            bookmarkService.removeBookmark(userId, snippetId);
            redirectAttributes.addFlashAttribute("message", "북마크에서 제거되었습니다.");
        } catch (Exception e) {
            log.error("북마크 제거 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("error", "북마크 제거 중 오류가 발생했습니다.");
        }

        return "redirect:/bookmark";
    }

    // 전체 스니펫 목록 조회 (북마크 여부 포함) - 수정된 메서드
    @GetMapping("/snippets")
    public String getAllSnippetsWithBookmarkStatus(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        log.info("전체 스니펫 목록 조회 - 사용자 ID: {}", userId);

        // 전체 스니펫 조회
        List<Snippets> allSnippets = bookmarkService.getAllSnippets();
        log.info("전체 스니펫 수: {}", allSnippets.size());

        if (userId != null) {
            // 사용자가 북마크한 스니펫 ID 목록 조회
            List<Snippets> bookmarkedSnippets = bookmarkService.getAllBookmarkByUserId(userId);
            Set<Long> bookmarkedSnippetIds = bookmarkedSnippets.stream()
                    .map(Snippets::getSnippetId)
                    .collect(Collectors.toSet());

            log.info("사용자 {}의 북마크된 스니펫 ID: {}", userId, bookmarkedSnippetIds);

            // 모델에 북마크된 스니펫 ID 목록 추가
            model.addAttribute("bookmarkedSnippetIds", bookmarkedSnippetIds);
        } else {
            model.addAttribute("bookmarkedSnippetIds", new HashSet<>());
        }

        model.addAttribute("snippets", allSnippets);
        model.addAttribute("userId", userId);

        return "bookmark/snippets-with-bookmark";
    }

    // 특정 사용자가 작성한 스니펫 목록 조회 (추가)
    @GetMapping("/my-snippets")
    public String getMySnippetsWithBookmarkStatus(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        log.info("내가 작성한 스니펫 목록 조회 - 사용자 ID: {}", userId);

        if (userId == null) {
            model.addAttribute("snippets", new ArrayList<>());
            model.addAttribute("userId", null);
            model.addAttribute("bookmarkedSnippetIds", new HashSet<>());
            return "bookmark/snippets-with-bookmark";
        }

        // 내가 작성한 스니펫 조회
        List<Snippets> mySnippets = bookmarkService.getSnippetsByUserId(userId);
        log.info("사용자 {}가 작성한 스니펫 수: {}", userId, mySnippets.size());

        // 내가 북마크한 스니펫 ID 목록 조회
        List<Snippets> bookmarkedSnippets = bookmarkService.getAllBookmarkByUserId(userId);
        Set<Long> bookmarkedSnippetIds = bookmarkedSnippets.stream()
                .map(Snippets::getSnippetId)
                .collect(Collectors.toSet());

        model.addAttribute("snippets", mySnippets);
        model.addAttribute("userId", userId);
        model.addAttribute("bookmarkedSnippetIds", bookmarkedSnippetIds);

        return "bookmark/snippets-with-bookmark";
    }
}