package kr.or.kosa.snippets.controller;

import kr.or.kosa.snippets.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
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
                               Model model) {

        System.out.println("좋아요 페이지 접근 - snippetId: " + snippetId);

        // 해당 스니펫의 좋아요 수 조회
        long likeCount = likeService.getLikesCount(snippetId);
        boolean isLiked = likeService.isLiked(snippetId);

        System.out.println("스니펫 " + snippetId + " - 좋아요 수: " + likeCount + ", 좋아요 상태: " + isLiked);

        model.addAttribute("snippetId", snippetId);
        model.addAttribute("likeCount", likeCount);
        model.addAttribute("isLiked", isLiked);

        return "like";  // like.jsp 파일을 반환
    }
}