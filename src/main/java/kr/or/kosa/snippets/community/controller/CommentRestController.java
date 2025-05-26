package kr.or.kosa.snippets.community.controller;

import kr.or.kosa.snippets.community.model.Comment;
import kr.or.kosa.snippets.community.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
                                                           @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserId = null;
        if (userDetails != null) {
            // currentUserId = ((CustomUserDetails) userDetails).getUserId();
        }

        List<Comment> comments = commentService.getCommentsByPostId(postId, currentUserId);

        Map<String, Object> response = new HashMap<>();
        response.put("comments", comments);

        return ResponseEntity.ok(response);
    }

    // 댓글 추가
    @PostMapping("/comment/add")
    public ResponseEntity<Map<String, Object>> addComment(@RequestBody Comment comment,
                                                          @AuthenticationPrincipal UserDetails userDetails) {
        // comment.setUserId(((CustomUserDetails) userDetails).getUserId());

        Integer commentId = commentService.addComment(comment);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("commentId", commentId);

        return ResponseEntity.ok(response);
    }

    // 댓글 수정
    @PostMapping("/comment/update")
    public ResponseEntity<Map<String, Object>> updateComment(@RequestBody Comment comment,
                                                             @AuthenticationPrincipal UserDetails userDetails) {
        // Long currentUserId = ((CustomUserDetails) userDetails).getUserId();
        Comment existingComment = commentService.getCommentById(comment.getCommentId());

        if (existingComment == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "댓글을 찾을 수 없습니다."));
        }

        // 댓글 작성자만 수정 가능
        // if (!existingComment.getUserId().equals(currentUserId)) {
        //     return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", "권한이 없습니다."));
        // }

        commentService.updateComment(comment);

        return ResponseEntity.ok(Map.of("success", true));
    }

    // 댓글 삭제
    @PostMapping("/comment/delete/{commentId}")
    public ResponseEntity<Map<String, Object>> deleteComment(@PathVariable Integer commentId,
                                                             @AuthenticationPrincipal UserDetails userDetails) {
        // Long currentUserId = ((CustomUserDetails) userDetails).getUserId();
        Comment existingComment = commentService.getCommentById(commentId);

        if (existingComment == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "댓글을 찾을 수 없습니다."));
        }

        // 댓글 작성자만 삭제 가능
        // if (!existingComment.getUserId().equals(currentUserId)) {
        //     return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", "권한이 없습니다."));
        // }

        commentService.deleteComment(commentId);

        return ResponseEntity.ok(Map.of("success", true));
    }

    // 댓글 좋아요/싫어요
    @PostMapping("/comment/like")
    public ResponseEntity<Map<String, Object>> toggleCommentLike(@RequestParam Integer commentId,
                                                                 @RequestParam boolean isLike,
                                                                 @AuthenticationPrincipal UserDetails userDetails) {
        // Long currentUserId = ((CustomUserDetails) userDetails).getUserId();
        Long currentUserId = 1L; // 임시

        commentService.toggleCommentLike(commentId, currentUserId, isLike);

        return ResponseEntity.ok(Map.of("success", true));
    }
}