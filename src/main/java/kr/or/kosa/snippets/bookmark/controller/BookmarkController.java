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

    // private 헬퍼 메서드 - 로그인 확인 로직 개선
    private Long requireLogin(CustomUserDetails userDetails) {
        if (userDetails == null) {
            log.warn("사용자 인증 정보가 없습니다.");
            return null;
        }
        Long userId = userDetails.getUserId();
        log.info("인증된 사용자 ID: {}", userId);
        return userId;
    }

    //사용자별 북마크 조회
    @GetMapping
    public String getAllBookmarksByUserId(@AuthenticationPrincipal CustomUserDetails userDetails,
                                          Model model) {
        Long currentId = requireLogin(userDetails);
        log.info("북마크 목록 조회 - 사용자 ID: {}", currentId);

        // 사이드바 활성화를 위한 activeMenu 설정
        model.addAttribute("activeMenu", "bookmarks");

        // 로그인 상태 확인을 위한 모델 속성 추가
        model.addAttribute("userId", currentId);

        if (currentId == null) {
            // 로그인하지 않은 경우
            model.addAttribute("bookmarks", new ArrayList<>());
            model.addAttribute("count", 0);
            log.warn("로그인하지 않은 사용자의 북마크 페이지 접근");
            return "bookmark/temp-bookmark-list";
        }

        try {
            List<Snippets> bookmarkList = bookmarkService.getAllBookmarkByUserId(currentId);
            log.info("조회된 북마크 수: {}", bookmarkList != null ? bookmarkList.size() : 0);

            // null 체크 추가
            if (bookmarkList == null) {
                bookmarkList = new ArrayList<>();
            }

            model.addAttribute("bookmarks", bookmarkList);
            model.addAttribute("count", bookmarkList.size());

        } catch (Exception e) {
            log.error("북마크 목록 조회 중 오류 발생", e);
            model.addAttribute("error", "북마크 목록을 불러오는 중 오류가 발생했습니다.");
            model.addAttribute("bookmarks", new ArrayList<>());
            model.addAttribute("count", 0);
        }

        return "bookmark/temp-bookmark-list";
    }

    //북마크된 스니펫 상세보기 (추가)
    @GetMapping("/snippet/{snippetId}")
    public String getBookmarkedSnippetDetail(@PathVariable Long snippetId,
                                             @AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

        Long currentId = requireLogin(userDetails);
        log.info("북마크된 스니펫 상세보기 - 사용자 ID: {}, 스니펫 ID: {}", currentId, snippetId);

        // 사이드바 활성화를 위한 activeMenu 설정
        model.addAttribute("activeMenu", "bookmarks");

        if (currentId == null) {
            log.warn("로그인하지 않은 사용자의 북마크 스니펫 상세보기 접근");
            return "redirect:/login";
        }

        try {
            // 북마크된 스니펫인지 확인
            if (!bookmarkService.isBookmarked(currentId, snippetId)) {
                log.warn("북마크되지 않은 스니펫 접근 시도 - 사용자 ID: {}, 스니펫 ID: {}", currentId, snippetId);
                return "redirect:/bookmark"; // 북마크되지 않은 스니펫이면 북마크 목록으로
            }

            Snippets snippet = bookmarkService.getSnippetById(snippetId);
            if (snippet == null) {
                log.warn("존재하지 않는 스니펫 접근 - 스니펫 ID: {}", snippetId);
                return "error/404";
            }

            model.addAttribute("snippet", snippet);
            model.addAttribute("isBookmarked", true);
            model.addAttribute("userId", currentId);

        } catch (Exception e) {
            log.error("북마크된 스니펫 상세보기 중 오류 발생", e);
            model.addAttribute("error", "스니펫을 불러오는 중 오류가 발생했습니다.");
        }

        return "bookmark/snippet-detail"; // 북마크용 상세 페이지
    }

    //북마크에서 제거 (POST 방식)
    @PostMapping("/remove/{snippetId}")
    public String removeBookmarkFromList(@PathVariable Long snippetId,
                                         @AuthenticationPrincipal CustomUserDetails userDetails,
                                         RedirectAttributes redirectAttributes) {

        Long currentId = requireLogin(userDetails);
        log.info("북마크 제거 - 사용자 ID: {}, 스니펫 ID: {}", currentId, snippetId);

        if (currentId == null) {
            log.warn("로그인하지 않은 사용자의 북마크 제거 시도");
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        try {
            bookmarkService.removeBookmark(currentId, snippetId);
            log.info("북마크 제거 성공 - 사용자 ID: {}, 스니펫 ID: {}", currentId, snippetId);
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

        // 사이드바 활성화를 위한 activeMenu 설정
        model.addAttribute("activeMenu", "bookmarks");

        // 로그인 상태 확인을 위한 모델 속성 추가
        model.addAttribute("userId", currentId);

        if (currentId == null) {
            log.warn("로그인하지 않은 사용자의 내 스니펫 페이지 접근");
            model.addAttribute("snippets", new ArrayList<>());
            model.addAttribute("bookmarkedSnippetIds", new HashSet<>());
            model.addAttribute("count", 0);
            return "bookmark/snippets-with-bookmark";
        }

        try {
            // SnippetService를 통해 내가 작성한 스니펫 조회 (완전한 정보 포함)
            List<Snippets> mySnippets = bookmarkService.getSnippetsByUserId(currentId);
            log.info("사용자 {}가 작성한 스니펫 수: {}", currentId, mySnippets != null ? mySnippets.size() : 0);

            // null 체크 추가
            if (mySnippets == null) {
                mySnippets = new ArrayList<>();
            }

            // 디버깅을 위한 추가 로그
            if (!mySnippets.isEmpty()) {
                log.info("첫 번째 스니펫 정보: ID={}, 타입={}, 메모={}",
                        mySnippets.get(0).getSnippetId(),
                        mySnippets.get(0).getType(),
                        mySnippets.get(0).getMemo());
            }

            // 내가 북마크한 스니펫 ID 목록 조회
            List<Snippets> bookmarkedSnippets = bookmarkService.getAllBookmarkByUserId(currentId);
            Set<Long> bookmarkedSnippetIds = new HashSet<>();

            if (bookmarkedSnippets != null) {
                bookmarkedSnippetIds = bookmarkedSnippets.stream()
                        .map(Snippets::getSnippetId)
                        .collect(Collectors.toSet());
            }

            log.info("북마크한 스니펫 ID 목록: {}", bookmarkedSnippetIds);

            model.addAttribute("snippets", mySnippets);
            model.addAttribute("bookmarkedSnippetIds", bookmarkedSnippetIds);
            model.addAttribute("count", mySnippets.size());

        } catch (Exception e) {
            log.error("내 스니펫 목록 조회 중 오류 발생", e);
            model.addAttribute("error", "스니펫 목록을 불러오는 중 오류가 발생했습니다.");
            model.addAttribute("snippets", new ArrayList<>());
            model.addAttribute("bookmarkedSnippetIds", new HashSet<>());
            model.addAttribute("count", 0);
        }

        return "bookmark/snippets-with-bookmark";
    }
}