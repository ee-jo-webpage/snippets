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
            // 인증 검증 (Spring Security가 이미 처리하지만 이중 체크)
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "인증이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }

            // 입력값 검증
            if (snippetId == null || snippetId <= 0) {
                response.put("success", false);
                response.put("message", "유효하지 않은 스니펫 ID입니다.");
                return ResponseEntity.badRequest().body(response);
            }

            Long userId = userDetails.getUserId();

            // 보안 로그
            System.out.println("좋아요 추가 시도 - userId: " + userId + ", snippetId: " + snippetId + ", IP: " + getClientIP());

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

    // 좋아요 취소 - 인증 필수
    @PostMapping("/remove")
    public ResponseEntity<Map<String, Object>> removeLike(@RequestParam Integer snippetId,
                                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "인증이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }

            if (snippetId == null || snippetId <= 0) {
                response.put("success", false);
                response.put("message", "유효하지 않은 스니펫 ID입니다.");
                return ResponseEntity.badRequest().body(response);
            }

            Long userId = userDetails.getUserId();

            // 보안 로그
            System.out.println("좋아요 취소 시도 - userId: " + userId + ", snippetId: " + snippetId + ", IP: " + getClientIP());

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

    // 특정 스니펫의 좋아요 수 조회 - 공개 접근 허용
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getLikesCount(@RequestParam Integer snippetId,
                                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (snippetId == null || snippetId <= 0) {
                response.put("success", false);
                response.put("message", "유효하지 않은 스니펫 ID입니다.");
                return ResponseEntity.badRequest().body(response);
            }

            long count = likeService.getLikesCount(snippetId);
            boolean isLiked = false;

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

    // 사용자가 좋아요한 스니펫 목록 - 인증 필수
    @GetMapping("/user")
    public ResponseEntity<?> getUserLikes(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "인증이 필요합니다.");
            return ResponseEntity.status(401).body(response);
        }

        List<Like> userLikes = likeService.getUserLikes(userDetails.getUserId());
        return ResponseEntity.ok(userLikes);
    }

    // 특정 스니펫의 좋아요 상태 확인 - 공개 접근 허용
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getLikeStatus(@RequestParam Integer snippetId,
                                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (snippetId == null || snippetId <= 0) {
                response.put("success", false);
                response.put("message", "유효하지 않은 스니펫 ID입니다.");
                return ResponseEntity.badRequest().body(response);
            }

            long count = likeService.getLikesCount(snippetId);
            boolean isLiked = false;

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

    // 클라이언트 IP 주소 가져오기 (보안 로그용)
    private String getClientIP() {
        // 실제 구현에서는 HttpServletRequest를 주입받아 사용
        return "unknown";
    }
}