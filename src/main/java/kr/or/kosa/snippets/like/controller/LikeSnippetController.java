package kr.or.kosa.snippets.like.controller;

import kr.or.kosa.snippets.config.AppConfig;
import kr.or.kosa.snippets.like.mapper.LikeSnippetMapper;
import kr.or.kosa.snippets.like.model.Snippet;
import kr.or.kosa.snippets.like.model.LikeTag;
import kr.or.kosa.snippets.like.service.LikeService;
import kr.or.kosa.snippets.like.service.LikeSnippetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
public class LikeSnippetController {

    @Autowired
    private LikeSnippetMapper likeSnippetMapper;

    @Autowired
    private LikeService likeService;

    @Autowired
    private LikeSnippetService likeSnippetService;

    @GetMapping("/snippet/{id}")
    public String showSnippetDetail(@PathVariable("id") Integer snippetId, Model model) {
        Integer currentUserId = AppConfig.getFixedUserId();

        // 스니펫 상세 정보 조회
        Snippet snippet = likeSnippetMapper.getSnippetDetailById(snippetId);

        if (snippet == null) {
            return "redirect:/popular-snippets"; // 스니펫이 없으면 인기 스니펫 페이지로 리다이렉트
        }

        // 스니펫 태그 조회
        List<LikeTag> likeTags = likeSnippetService.getTagsBySnippetId(snippetId);

        // 좋아요 상태 확인
        boolean isLiked = likeService.isLiked(snippetId);

        // 실제 좋아요 수 업데이트
        long actualLikeCount = likeService.getLikesCount(snippetId);
        snippet.setLikeCount((int) actualLikeCount);

        model.addAttribute("snippet", snippet);
        model.addAttribute("tags", likeTags);
        model.addAttribute("isLiked", isLiked);
        model.addAttribute("currentUserId", currentUserId);

        return "like/snippet-detail";
    }

    // 인기 스니펫 목록 페이지 (상위 100개 제한, 페이징 처리)
    @GetMapping("/popular-snippets")
    public String showPopularSnippets(
        @RequestParam(value = "page", defaultValue = "1") int page,
        Model model) {

        Integer currentUserId = AppConfig.getFixedUserId();
        int pageSize = 8; // 페이지당 8개 스니펫 표시
        int offset = (page - 1) * pageSize;

        // 뷰를 통해 페이징 처리된 인기 스니펫 조회 (상위 100개 내에서만)
        List<Snippet> snippets = likeSnippetService.getPopularSnippetsPagedFromView(offset, pageSize);
        int totalSnippets = likeSnippetService.countPopularSnippetsFromView(); // 최대 100개

        // 뷰 실패 시 기존 방식으로 페이징 대체
        if (snippets == null || snippets.isEmpty()) {
            // 기존 방식으로 상위 100개 중에서 페이징
            List<Snippet> allPopularSnippets = likeSnippetService.getPopularSnippets(100);
            totalSnippets = Math.min(allPopularSnippets.size(), 100);

            int fromIndex = Math.min(offset, allPopularSnippets.size());
            int toIndex = Math.min(offset + pageSize, allPopularSnippets.size());
            snippets = allPopularSnippets.subList(fromIndex, toIndex);
        }

        // 각 스니펫의 좋아요 상태 확인
        Map<Integer, Boolean> likeStatusMap = new HashMap<>();
        for (Snippet snippet : snippets) {
            boolean isLiked = likeService.isLiked(snippet.getSnippetId());
            likeStatusMap.put(snippet.getSnippetId(), isLiked);
        }

        // 페이징 정보 계산
        int totalPages = (int) Math.ceil((double) totalSnippets / pageSize);

        model.addAttribute("snippets", snippets);
        model.addAttribute("likeStatusMap", likeStatusMap);
        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("currentSort", "popular");
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalSnippets", totalSnippets);

        // 검색이나 정렬 기능 없이 인기 스니펫만 표시
        model.addAttribute("hasSearchFilter", false);

        return "like/snippets";
    }

}