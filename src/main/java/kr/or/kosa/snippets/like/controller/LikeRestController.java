package kr.or.kosa.snippets.like.controller;

import kr.or.kosa.snippets.like.model.Like;
import kr.or.kosa.snippets.like.service.LikeService;
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
        // @RequestParam: POST 요청의 Form 데이터에서 snippetId 값을 받음
        // 반환 타입: Map<String, Object> → JSON 형태로 변환됨

        Map<String, Object> response = new HashMap<>();
        // 클라이언트에게 보낼 응답 데이터를 담는 맵 생성

        try {
            likeService.addLike(snippetId);
            long newCount = likeService.getLikesCount(snippetId);

            response.put("success", true);
            // 성공 여부를 true로 설정
            response.put("likeCount", newCount);
            // 새로운 좋아요 수를 응답에 포함
            response.put("message", "좋아요가 추가되었습니다.");
            // 사용자에게 보여줄 메시지

            System.out.println("좋아요 추가 성공 - snippetId: " + snippetId + ", 새로운 좋아요 수: " + newCount);

        } catch (Exception e) {
            response.put("success", false);
            // 성공 여부를 false로 설정
            response.put("message", "좋아요 추가 실패: " + e.getMessage());
            // 에러 메시지를 포함
            System.err.println("좋아요 추가 실패: " + e.getMessage());

        }
        return response;
        // 맵이 JSON으로 변환되어 클라이언트에게 전송됨
    }

    // 좋아요 취소
    @PostMapping("/remove")
    public Map<String, Object> removeLike(@RequestParam Integer snippetId) {

        Map<String, Object> response = new HashMap<>();

        try {
            likeService.removeLike(snippetId);
            // 좋아요 취소 비즈니스 로직 실행

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
            // 좋아요 수와 상태를 모두 응답에 포함

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "조회 실패: " + e.getMessage());
        }
        return response;
    }

    // 사용자가 좋아요한 스니펫 목록
    @GetMapping("/user")
    public List<Like> getUserLikes() {
        // 반환 타입이 List<Like> → JSON 배열로 변환됨

        return likeService.getUserLikes();
        // 현재 사용자가 좋아요한 모든 스니펫의 Like 객체 리스트 반환
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