package kr.or.kosa.snippets.bookmark.controller;

import jakarta.servlet.http.HttpSession;
import kr.or.kosa.snippets.basic.model.Snippets;
import kr.or.kosa.snippets.bookmark.model.Bookmark;
import kr.or.kosa.snippets.bookmark.service.BookmarkService;
import kr.or.kosa.snippets.like.mapper.SnippetContentMapper;
import kr.or.kosa.snippets.like.model.LikeTag;
import kr.or.kosa.snippets.like.service.LikeService;
import kr.or.kosa.snippets.like.service.LikeSnippetService;
import kr.or.kosa.snippets.like.service.LikeUserService;
import kr.or.kosa.snippets.user.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkRestController {

    @Autowired
    BookmarkService bookmarkService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private LikeSnippetService likeSnippetService;

    @Autowired
    private LikeUserService likeUserService;

    @Autowired
    private SnippetContentMapper snippetContentMapper;

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

    /**
     * 북마크된 스니펫 목록 조회 (상세정보 포함)
     */
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

            if (bookmarkList == null || bookmarkList.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "snippets", new ArrayList<>(),
                        "count", 0
                ));
            }

            // 각 스니펫의 상세정보 추가
            List<Map<String, Object>> enhancedSnippets = new ArrayList<>();

            for (Snippets snippet : bookmarkList) {
                Map<String, Object> snippetData = new HashMap<>();

                // 기본 스니펫 정보
                snippetData.put("snippet", snippet);

                // 태그 정보 조회
                List<LikeTag> tags = likeSnippetService.getTagsBySnippetId(snippet.getSnippetId());
                snippetData.put("tags", tags);

                // 좋아요 상태 확인
                boolean isLiked = likeService.isLiked(snippet.getSnippetId(), userId);
                snippetData.put("isLiked", isLiked);

                // 실제 좋아요 수 업데이트
                long actualLikeCount = likeService.getLikesCount(snippet.getSnippetId());
                snippetData.put("actualLikeCount", actualLikeCount);

                // 소유자 nickname 조회
                String ownerNickname = likeUserService.getNicknameByUserId(snippet.getUserId());
                snippetData.put("ownerNickname", ownerNickname);

                // 스니펫 타입별 실제 content 조회
                Object snippetContent = null;
                try {
                    switch (snippet.getType().toString().toUpperCase()) {
                        case "CODE":
                            snippetContent = snippetContentMapper.getSnippetCodeById(snippet.getSnippetId());
                            break;
                        case "TEXT":
                            snippetContent = snippetContentMapper.getSnippetTextById(snippet.getSnippetId());
                            break;
                        case "IMG":
                            snippetContent = snippetContentMapper.getSnippetImageById(snippet.getSnippetId());
                            break;
                    }
                } catch (Exception e) {
                    log.error("스니펫 content 조회 실패 - 스니펫 ID: {}, 오류: {}", snippet.getSnippetId(), e.getMessage());
                }
                snippetData.put("snippetContent", snippetContent);

                // 북마크 상태 (항상 true)
                snippetData.put("isBookmarked", true);

                enhancedSnippets.add(snippetData);
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "snippets", enhancedSnippets,
                    "count", enhancedSnippets.size()
            ));

        } catch (Exception e) {
            log.error("북마크된 스니펫 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "북마크 목록 조회 중 오류가 발생했습니다."));
        }
    }

    /**
     * 특정 북마크된 스니펫의 상세정보 조회
     */
    @GetMapping("/snippet/{snippetId}/detail")
    public ResponseEntity<?> getBookmarkedSnippetDetail(@PathVariable Long snippetId,
                                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long userId = requireLogin(userDetails);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            log.info("북마크된 스니펫 상세정보 조회 - 사용자 ID: {}, 스니펫 ID: {}", userId, snippetId);

            // 북마크된 스니펫인지 확인
            if (!bookmarkService.isBookmarked(userId, snippetId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "북마크되지 않은 스니펫입니다."));
            }

            Snippets snippet = bookmarkService.getSnippetById(snippetId);
            if (snippet == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "스니펫을 찾을 수 없습니다."));
            }

            Map<String, Object> result = new HashMap<>();

            // 기본 스니펫 정보
            result.put("snippet", snippet);

            // 스니펫 태그 조회
            List<LikeTag> tags = likeSnippetService.getTagsBySnippetId(snippetId);
            result.put("tags", tags);

            // 좋아요 상태 확인
            boolean isLiked = likeService.isLiked(snippetId, userId);
            result.put("isLiked", isLiked);

            // 실제 좋아요 수 업데이트
            long actualLikeCount = likeService.getLikesCount(snippetId);
            result.put("actualLikeCount", actualLikeCount);

            // 소유자 nickname 조회
            String ownerNickname = likeUserService.getNicknameByUserId(snippet.getUserId());
            result.put("ownerNickname", ownerNickname);

            // 스니펫 타입별 실제 content 조회
            Object snippetContent = null;
            try {
                switch (snippet.getType().toString().toUpperCase()) {
                    case "CODE":
                        snippetContent = snippetContentMapper.getSnippetCodeById(snippetId);
                        break;
                    case "TEXT":
                        snippetContent = snippetContentMapper.getSnippetTextById(snippetId);
                        break;
                    case "IMG":
                        snippetContent = snippetContentMapper.getSnippetImageById(snippetId);
                        break;
                }
            } catch (Exception e) {
                log.error("스니펫 content 조회 실패 - 스니펫 ID: {}, 오류: {}", snippetId, e.getMessage());
            }
            result.put("snippetContent", snippetContent);

            // 북마크 상태 (항상 true)
            result.put("isBookmarked", true);

            // 현재 사용자 정보
            result.put("currentUserId", userId);
            result.put("currentUserNickname", userDetails.getNickname());
            result.put("isLoggedIn", true);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", result
            ));

        } catch (Exception e) {
            log.error("북마크된 스니펫 상세정보 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "스니펫 상세정보 조회 중 오류가 발생했습니다."));
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