package kr.or.kosa.snippets.bookmark.controller;

import jakarta.servlet.http.HttpSession;
import kr.or.kosa.snippets.basic.model.Snippets;
import kr.or.kosa.snippets.bookmark.model.Bookmark;
import kr.or.kosa.snippets.bookmark.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkRestController {

    @Autowired
    BookmarkService bookmarkService;

    //북마크 토글 (추가/삭제)
    @PostMapping("/toggle")
    public ResponseEntity<?> toggleBookmark(@RequestParam Long snippetId, HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            boolean isBookmarked = bookmarkService.toggleBookmark(userId, snippetId);
            String message = isBookmarked ? "북마크가 추가되었습니다." : "북마크가 제거되었습니다.";

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "bookmarked", isBookmarked,
                    "message", message
            ));

        } catch (Exception e) {
            log.error("북마크 토글 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "북마크 처리 중 오류가 발생했습니다."));
        }
    }

    //북마크 추가 (기존 메서드 수정)
    @PostMapping("/add")
    public ResponseEntity<?> addBookmark(@RequestParam Long snippetId, HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            // 이미 북마크되어 있는지 확인
            if (bookmarkService.isBookmarked(userId, snippetId)) {
                return ResponseEntity.ok(Map.of("success", false, "message", "이미 북마크된 스니펫입니다."));
            }

            bookmarkService.addBookmark(userId, snippetId);
            return ResponseEntity.ok(Map.of("success", true, "message", "북마크가 추가되었습니다."));

        } catch (Exception e) {
            log.error("북마크 추가 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "북마크 추가 중 오류가 발생했습니다."));
        }
    }

    //북마크 삭제
    @DeleteMapping("/remove")
    public ResponseEntity<?> removeBookmark(@RequestParam Long snippetId, HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            if (!bookmarkService.isBookmarked(userId, snippetId)) {
                return ResponseEntity.ok(Map.of("success", false, "message", "북마크되지 않은 스니펫입니다."));
            }

            bookmarkService.removeBookmark(userId, snippetId);
            return ResponseEntity.ok(Map.of("success", true, "message", "북마크가 제거되었습니다."));

        } catch (Exception e) {
            log.error("북마크 삭제 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "북마크 삭제 중 오류가 발생했습니다."));
        }
    }

    //북마크 여부 확인
    @GetMapping("/check")
    public ResponseEntity<?> checkBookmark(@RequestParam Long snippetId, HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            boolean isBookmarked = bookmarkService.isBookmarked(userId, snippetId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "bookmarked", isBookmarked
            ));

        } catch (Exception e) {
            log.error("북마크 확인 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "북마크 확인 중 오류가 발생했습니다."));
        }
    }
}