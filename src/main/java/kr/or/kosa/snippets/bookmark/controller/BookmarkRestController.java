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

    //북마크 추가
    public ResponseEntity<?> addBookmark(@RequestBody Bookmark request, HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            // 이미 북마크되어 있는지 확인
            if (bookmarkService.isBookmarked(userId, request.getSnippetId())) {
                return ResponseEntity.ok(Map.of("success", false, "message", "이미 북마크된 스니펫입니다."));
            }

            bookmarkService.addBookmark(userId, request.getSnippetId());
            return ResponseEntity.ok(Map.of("success", true, "message", "북마크가 추가되었습니다."));

        } catch (Exception e) {
            log.error("북마크 추가 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "북마크 추가 중 오류가 발생했습니다."));
        }
    }

}
