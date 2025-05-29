package kr.or.kosa.snippets.like.controller;

import kr.or.kosa.snippets.like.service.LikeService;
import kr.or.kosa.snippets.like.service.LikeAccessControlService;
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

    @Autowired
    private LikeAccessControlService accessControlService;

    @GetMapping("/likePage")
    public String showLikePage(@RequestParam(value = "snippetId", defaultValue = "1") Long snippetId,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model) {

        System.out.println("좋아요 페이지 접근 - snippetId: " + snippetId);

        // 스니펫 접근 권한 체크
        if (!accessControlService.canAccessSnippet(snippetId, userDetails)) {
            accessControlService.logAccessDenied(snippetId, userDetails, "좋아요 페이지 접근 권한 없음");
            return "redirect:/popular-snippets?error=access_denied";
        }

        // 해당 스니펫의 좋아요 수 조회
        long likeCount = likeService.getLikesCount(snippetId);
        boolean isLiked = false;

        // 로그인한 사용자의 좋아요 여부 확인 (이미 인증된 상태이므로 userDetails는 null이 아님)
        isLiked = likeService.isLiked(snippetId, userDetails.getUserId());
        System.out.println("사용자 " + userDetails.getUserId() + " - 스니펫 " + snippetId + " 좋아요 상태: " + isLiked);

        System.out.println("스니펫 " + snippetId + " - 좋아요 수: " + likeCount + ", 좋아요 상태: " + isLiked);

        // 접근 성공 로그
        accessControlService.logAccessGranted(snippetId, userDetails, "좋아요 페이지 접근");

        // 모델에 데이터 추가
        model.addAttribute("snippetId", snippetId);
        model.addAttribute("likeCount", likeCount);
        model.addAttribute("isLiked", isLiked);
        model.addAttribute("isLoggedIn", true); // 이미 인증된 상태
        model.addAttribute("userId", userDetails.getUserId());
        model.addAttribute("currentUserNickname", userDetails.getNickname());

        return "like/like";  // like.html
    }
}