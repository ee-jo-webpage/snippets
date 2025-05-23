package kr.or.kosa.snippets.like.controller;

import kr.or.kosa.snippets.like.service.LikeService;
import kr.or.kosa.snippets.user.service.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LikeController {

    @Autowired
    private LikeService likeService;

    @GetMapping("/likePage")
    public String showLikePage(@RequestParam(value = "snippetId", defaultValue = "1") Integer snippetId,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model) {

        System.out.println("좋아요 페이지 접근 - snippetId: " + snippetId);

        // 해당 스니펫의 좋아요 수 조회
        long likeCount = likeService.getLikesCount(snippetId);
        boolean isLiked = false;

        // 로그인한 사용자의 좋아요 여부 확인
        if (userDetails != null) {
            isLiked = likeService.isLiked(snippetId, userDetails.getUserId());
            System.out.println("사용자 " + userDetails.getUserId() + " - 스니펫 " + snippetId + " 좋아요 상태: " + isLiked);
        } else {
            System.out.println("비로그인 사용자 - 좋아요 상태 확인 불가");
        }

        System.out.println("스니펫 " + snippetId + " - 좋아요 수: " + likeCount + ", 좋아요 상태: " + isLiked);

        // 모델에 데이터 추가
        model.addAttribute("snippetId", snippetId);
        model.addAttribute("likeCount", likeCount);
        model.addAttribute("isLiked", isLiked);
        model.addAttribute("isLoggedIn", userDetails != null);
        model.addAttribute("userId", userDetails != null ? userDetails.getUserId() : null);


        return "like/like";  // like.html
    }
}