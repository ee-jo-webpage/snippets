package kr.or.kosa.snippets.controller;

import kr.or.kosa.snippets.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Map<String, Object> addLike(@RequestParam Integer snippetId) {
        Map<String, Object> response = new HashMap<>();
        try {
            likeService.addLike(snippetId);
            long newCount = likeService.getLikesCount(snippetId);

            response.put("success", true);
            response.put("likeCount", newCount);
            response.put("message", "좋아요가 추가되었습니다.");

            System.out.println("좋아요 추가 성공 - snippetId: " + snippetId + ", 새로운 좋아요 수: " + newCount);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "좋아요 추가 실패: " + e.getMessage());
            System.err.println("좋아요 추가 실패: " + e.getMessage());
        }
        return response;
    }

    // 좋아요 취소
    @PostMapping("/remove")
    public Map<String, Object> removeLike(@RequestParam Integer snippetId) {
        Map<String, Object> response = new HashMap<>();
        try {
            likeService.removeLike(snippetId);
            long newCount = likeService.getLikesCount(snippetId);

            response.put("success", true);
            response.put("likeCount", newCount);
            response.put("message", "좋아요가 취소되었습니다.");

            System.out.println("좋아요 취소 성공 - snippetId: " + snippetId + ", 새로운 좋아요 수: " + newCount);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "좋아요 취소 실패: " + e.getMessage());
            System.err.println("좋아요 취소 실패: " + e.getMessage());
        }
        return response;
    }

    // 특정 스니펫의 좋아요 수 조회
    @GetMapping("/count")
    public Map<String, Object> getLikesCount(@RequestParam Integer snippetId) {
        Map<String, Object> response = new HashMap<>();
        try {
            long count = likeService.getLikesCount(snippetId);
            boolean isLiked = likeService.isLiked(snippetId);

            response.put("success", true);
            response.put("likeCount", count);
            response.put("isLiked", isLiked);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "조회 실패: " + e.getMessage());
        }
        return response;
    }

    // 사용자가 좋아요한 스니펫 목록
    @GetMapping("/user")
    public List<kr.or.kosa.snippets.model.Like> getUserLikes() {
        return likeService.getUserLikes();
    }

    // 특정 스니펫의 좋아요 상태 확인
    @GetMapping("/status")
    public Map<String, Object> getLikeStatus(@RequestParam Integer snippetId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean isLiked = likeService.isLiked(snippetId);
            long count = likeService.getLikesCount(snippetId);

            response.put("success", true);
            response.put("isLiked", isLiked);
            response.put("likeCount", count);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "상태 조회 실패: " + e.getMessage());
        }
        return response;
    }
}