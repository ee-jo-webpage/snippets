package kr.or.kosa.snippets.like.controller;

import kr.or.kosa.snippets.like.model.Like;
import kr.or.kosa.snippets.like.service.LikeService;
import kr.or.kosa.snippets.user.service.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/likes")
public class LikeRestController {

    @Autowired
    private LikeService likeService;

    // 좋아요 추가
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addLike(@RequestParam Integer snippetId,
                                                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 로그인 확인
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }

            Long userId = userDetails.getUserId();
            likeService.addLike(snippetId, userId);
            long newCount = likeService.getLikesCount(snippetId);

            response.put("success", true);
            response.put("likeCount", newCount);
            response.put("message", "좋아요가 추가되었습니다.");

            System.out.println("좋아요 추가 성공 - userId: " + userId + ", snippetId: " + snippetId + ", 새로운 좋아요 수: " + newCount);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "좋아요 추가 실패: " + e.getMessage());
            System.err.println("좋아요 추가 실패: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    // 좋아요 취소
    @PostMapping("/remove")
    public ResponseEntity<Map<String, Object>> removeLike(@RequestParam Integer snippetId,
                                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 로그인 확인
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }

            Long userId = userDetails.getUserId();
            likeService.removeLike(snippetId, userId);
            long newCount = likeService.getLikesCount(snippetId);

            response.put("success", true);
            response.put("likeCount", newCount);
            response.put("message", "좋아요가 취소되었습니다.");

            System.out.println("좋아요 취소 성공 - userId: " + userId + ", snippetId: " + snippetId + ", 새로운 좋아요 수: " + newCount);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "좋아요 취소 실패: " + e.getMessage());
            System.err.println("좋아요 취소 실패: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    // 특정 스니펫의 좋아요 수 조회
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getLikesCount(@RequestParam Integer snippetId,
                                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            long count = likeService.getLikesCount(snippetId);
            boolean isLiked = false;

            // 로그인한 사용자의 좋아요 여부 확인
            if (userDetails != null) {
                isLiked = likeService.isLiked(snippetId, userDetails.getUserId());
            }

            response.put("success", true);
            response.put("likeCount", count);
            response.put("isLiked", isLiked);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "조회 실패: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    // 사용자가 좋아요한 스니펫 목록
    @GetMapping("/user")
    public ResponseEntity<?> getUserLikes(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(401).body(response);
        }

        List<Like> userLikes = likeService.getUserLikes(userDetails.getUserId());
        return ResponseEntity.ok(userLikes);
    }

    // 특정 스니펫의 좋아요 상태 확인
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getLikeStatus(@RequestParam Integer snippetId,
                                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        try {
            long count = likeService.getLikesCount(snippetId);
            boolean isLiked = false;

            // 로그인한 사용자의 좋아요 여부 확인
            if (userDetails != null) {
                isLiked = likeService.isLiked(snippetId, userDetails.getUserId());
            }

            response.put("success", true);
            response.put("isLiked", isLiked);
            response.put("likeCount", count);
            response.put("userId", userDetails != null ? userDetails.getUserId() : null);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "상태 조회 실패: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
}