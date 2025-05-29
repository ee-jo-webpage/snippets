package kr.or.kosa.snippets.bookmark.controller;

import jakarta.servlet.http.HttpSession;
import kr.or.kosa.snippets.basic.model.Snippets;
import kr.or.kosa.snippets.bookmark.model.Bookmark;
import kr.or.kosa.snippets.bookmark.service.BookmarkService;
import kr.or.kosa.snippets.user.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkRestController {

    @Autowired
    BookmarkService bookmarkService;

    // 로그인 확인 헬퍼 메서드
    private Long requireLogin(CustomUserDetails userDetails) {
        if (userDetails == null) {
            log.warn("사용자 인증 정보가 없습니다.");
            return null;
        }
        Long userId = userDetails.getUserId();
        log.info("REST API 인증된 사용자 ID: {}", userId);
        return userId;
    }

    @GetMapping("/snippets")
    public ResponseEntity<?> getBookmarkedSnippets(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long userId = requireLogin(userDetails);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            log.info("북마크된 스니펫 목록 조회 - 사용자 ID: {}", userId);

            List<Snippets> bookmarkList = bookmarkService.getAllBookmarkByUserId(userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "snippets", bookmarkList != null ? bookmarkList : new ArrayList<>(),
                    "count", bookmarkList != null ? bookmarkList.size() : 0
            ));

        } catch (Exception e) {
            log.error("북마크된 스니펫 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "북마크 목록 조회 중 오류가 발생했습니다."));
        }
    }

    //북마크 토글 (추가/삭제)
    @PostMapping("/toggle")
    public ResponseEntity<?> toggleBookmark(@RequestParam Long snippetId,
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long userId = requireLogin(userDetails);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            log.info("북마크 토글 - 사용자 ID: {}, 스니펫 ID: {}", userId, snippetId);

            boolean isBookmarked = bookmarkService.toggleBookmark(userId, snippetId);
            String message = isBookmarked ? "북마크가 추가되었습니다." : "북마크가 제거되었습니다.";

            log.info("북마크 토글 결과 - 사용자 ID: {}, 스니펫 ID: {}, 북마크됨: {}", userId, snippetId, isBookmarked);

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

    //북마크 추가
    @PostMapping
    public ResponseEntity<?> addBookmark(@RequestBody Map<String, Long> request,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long userId = requireLogin(userDetails);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            Long snippetId = request.get("snippetId");
            if (snippetId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "스니펫 ID가 필요합니다."));
            }

            log.info("북마크 추가 - 사용자 ID: {}, 스니펫 ID: {}", userId, snippetId);

            // 이미 북마크되어 있는지 확인
            if (bookmarkService.isBookmarked(userId, snippetId)) {
                return ResponseEntity.ok(Map.of("success", false, "message", "이미 북마크된 스니펫입니다."));
            }

            bookmarkService.addBookmark(userId, snippetId);
            log.info("북마크 추가 성공 - 사용자 ID: {}, 스니펫 ID: {}", userId, snippetId);
            return ResponseEntity.ok(Map.of("success", true, "message", "북마크가 추가되었습니다."));

        } catch (Exception e) {
            log.error("북마크 추가 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "북마크 추가 중 오류가 발생했습니다."));
        }
    }

    //북마크 삭제
    @DeleteMapping("/{snippetId}")
    public ResponseEntity<?> removeBookmark(@PathVariable Long snippetId,
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long userId = requireLogin(userDetails);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            log.info("북마크 삭제 - 사용자 ID: {}, 스니펫 ID: {}", userId, snippetId);

            if (!bookmarkService.isBookmarked(userId, snippetId)) {
                return ResponseEntity.ok(Map.of("success", false, "message", "북마크되지 않은 스니펫입니다."));
            }

            bookmarkService.removeBookmark(userId, snippetId);
            log.info("북마크 삭제 성공 - 사용자 ID: {}, 스니펫 ID: {}", userId, snippetId);
            return ResponseEntity.ok(Map.of("success", true, "message", "북마크가 제거되었습니다."));

        } catch (Exception e) {
            log.error("북마크 삭제 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "북마크 삭제 중 오류가 발생했습니다."));
        }
    }

    // 기존 삭제 API (하위 호환성)
    @DeleteMapping("/remove")
    public ResponseEntity<?> removeBookmarkLegacy(@RequestParam Long snippetId,
                                                  @AuthenticationPrincipal CustomUserDetails userDetails) {
        return removeBookmark(snippetId, userDetails);
    }

    //북마크 여부 확인
    @GetMapping("/check/{snippetId}")
    public ResponseEntity<?> checkBookmark(@PathVariable Long snippetId,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long userId = requireLogin(userDetails);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            log.debug("북마크 상태 확인 - 사용자 ID: {}, 스니펫 ID: {}", userId, snippetId);

            boolean isBookmarked = bookmarkService.isBookmarked(userId, snippetId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "isBookmarked", isBookmarked
            ));

        } catch (Exception e) {
            log.error("북마크 확인 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "북마크 확인 중 오류가 발생했습니다."));
        }
    }
}