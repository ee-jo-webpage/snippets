package kr.or.kosa.snippets.community.controller;

import kr.or.kosa.snippets.community.model.Comment;
import kr.or.kosa.snippets.community.service.CommentService;
import kr.or.kosa.snippets.user.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/board")
@RequiredArgsConstructor
public class CommentRestController {
    private final CommentService commentService;

    // 댓글 목록 조회
    @GetMapping("/comments/{postId}")
    public ResponseEntity<Map<String, Object>> getComments(@PathVariable Integer postId,
                                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long currentUserId = null;
            if (userDetails != null) {
                currentUserId = userDetails.getUserId();
            }

            List<Comment> comments = commentService.getCommentsByPostId(postId, currentUserId);

            Map<String, Object> response = new HashMap<>();
            response.put("comments", comments);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("댓글 목록 조회 오류: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "댓글을 불러올 수 없습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // 댓글 추가
    @PostMapping("/comment/add")
    public ResponseEntity<Map<String, Object>> addComment(@RequestBody Comment comment,
                                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // 로그인 체크
            if (userDetails == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // 사용자 ID 설정
            comment.setUserId(userDetails.getUserId());

            // 댓글 등록
            Integer commentId = commentService.addComment(comment);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("commentId", commentId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("댓글 등록 오류: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "댓글 등록에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 댓글 수정
    @PostMapping("/comment/update")
    public ResponseEntity<Map<String, Object>> updateComment(@RequestBody Comment comment,
                                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // 로그인 체크
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            Long currentUserId = userDetails.getUserId();
            Comment existingComment = commentService.getCommentById(comment.getCommentId());

            if (existingComment == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "댓글을 찾을 수 없습니다."));
            }

            // 댓글 작성자만 수정 가능
            if (!existingComment.getUserId().equals(currentUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "권한이 없습니다."));
            }

            commentService.updateComment(comment);

            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            System.out.println("댓글 수정 오류: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "댓글 수정에 실패했습니다."));
        }
    }

    // 댓글 삭제
    @PostMapping("/comment/delete/{commentId}")
    public ResponseEntity<Map<String, Object>> deleteComment(@PathVariable Integer commentId,
                                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // 로그인 체크
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            Long currentUserId = userDetails.getUserId();
            Comment existingComment = commentService.getCommentById(commentId);

            if (existingComment == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "댓글을 찾을 수 없습니다."));
            }

            // 댓글 작성자만 삭제 가능
            if (!existingComment.getUserId().equals(currentUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "권한이 없습니다."));
            }

            commentService.deleteComment(commentId);

            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            System.out.println("댓글 삭제 오류: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "댓글 삭제에 실패했습니다."));
        }
    }

    // 댓글 좋아요/싫어요
    @PostMapping("/comment/like")
    public ResponseEntity<Map<String, Object>> toggleCommentLike(@RequestParam Integer commentId,
                                                                 @RequestParam boolean isLike,
                                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // 로그인 체크
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            Long currentUserId = userDetails.getUserId();
            commentService.toggleCommentLike(commentId, currentUserId, isLike);

            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            System.out.println("댓글 좋아요/싫어요 오류: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "좋아요 처리에 실패했습니다."));
        }
    }
}