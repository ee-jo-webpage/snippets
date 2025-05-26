package kr.or.kosa.snippets.bookmark.controller;

import jakarta.servlet.http.HttpSession;
import kr.or.kosa.snippets.basic.model.Snippets;
import kr.or.kosa.snippets.bookmark.model.Bookmark;
import kr.or.kosa.snippets.bookmark.service.BookmarkService;
import kr.or.kosa.snippets.user.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@RequestMapping("/bookmark")
@Slf4j
@Controller
@RequiredArgsConstructor
public class BookmarkController {

    @Autowired
    BookmarkService bookmarkService;

    // private 헬퍼 메서드
    private Long requireLogin(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다");
        }
        return userDetails.getUserId();
    }

    //사용자별 북마크 조회
    @GetMapping
    public String getAllBookmarksByUserId(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           Model model) {
        Long currentId = requireLogin(userDetails);
//        Long currentId = userDetails.getUserId();

        log.info("사용자 ID: {}", currentId);

        List<Snippets> bookmarkList = bookmarkService.getAllBookmarkByUserId(currentId);
        log.info("조회된 북마크 수: {}", bookmarkList.size());

        model.addAttribute("bookmarks", bookmarkList);
        model.addAttribute("count", bookmarkList.size());

        return "bookmark/bookmark-list";
    }

    //북마크된 스니펫 상세보기 (추가)
    @GetMapping("/snippet/{snippetId}")
    public String getBookmarkedSnippetDetail(@PathVariable Long snippetId,
                                             @AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

//        Long currentId = requireLogin(userDetails);
        Long currentId = userDetails.getUserId();

        // 북마크된 스니펫인지 확인
        if (!bookmarkService.isBookmarked(currentId, snippetId)) {
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
    public String removeBookmarkFromList(@PathVariable Long snippetId,
                                         @AuthenticationPrincipal CustomUserDetails userDetails,
                                         RedirectAttributes redirectAttributes) {

//        Long currentId = requireLogin(userDetails);
        Long currentId = userDetails.getUserId();

        try {
            bookmarkService.removeBookmark(currentId, snippetId);
            redirectAttributes.addFlashAttribute("message", "북마크에서 제거되었습니다.");
        } catch (Exception e) {
            log.error("북마크 제거 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("error", "북마크 제거 중 오류가 발생했습니다.");
        }

        return "redirect:/bookmark";
    }

    // 특정 사용자가 작성한 스니펫 목록 조회
    @GetMapping("/my-snippets")
    public String getMySnippetsWithBookmarkStatus(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

        Long currentId = requireLogin(userDetails);
        log.info("내가 작성한 스니펫 목록 조회 - 사용자 ID: {}", currentId);

        if (currentId == null) {
            model.addAttribute("snippets", new ArrayList<>());
            model.addAttribute("userId", null);
            model.addAttribute("bookmarkedSnippetIds", new HashSet<>());
            model.addAttribute("count", 0); // count 추가
            return "bookmark/snippets-with-bookmark";
        }

        // 내가 작성한 스니펫 조회
        List<Snippets> mySnippets = bookmarkService.getSnippetsByUserId(currentId);
        log.info("사용자 {}가 작성한 스니펫 수: {}", currentId, mySnippets.size());

        // 디버깅을 위한 추가 로그
        if (mySnippets != null && !mySnippets.isEmpty()) {
            log.info("첫 번째 스니펫 정보: ID={}, 메모={}",
                    mySnippets.get(0).getSnippetId(),
                    mySnippets.get(0).getMemo());
        }

        // 내가 북마크한 스니펫 ID 목록 조회
        List<Snippets> bookmarkedSnippets = bookmarkService.getAllBookmarkByUserId(currentId);
        Set<Long> bookmarkedSnippetIds = bookmarkedSnippets.stream()
                .map(Snippets::getSnippetId)
                .collect(Collectors.toSet());

        log.info("북마크한 스니펫 ID 목록: {}", bookmarkedSnippetIds);

        model.addAttribute("snippets", mySnippets);
        model.addAttribute("userId", currentId);
        model.addAttribute("bookmarkedSnippetIds", bookmarkedSnippetIds);
        model.addAttribute("count", mySnippets.size()); // count 추가

        return "bookmark/snippets-with-bookmark";
    }
}