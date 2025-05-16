package kr.or.kosa.snippets.controller;

import kr.or.kosa.snippets.config.AppConfig;
import kr.or.kosa.snippets.mapper.SnippetMapper;
import kr.or.kosa.snippets.model.Snippet;
import kr.or.kosa.snippets.model.Tag;
import kr.or.kosa.snippets.service.LikeService;
import kr.or.kosa.snippets.service.SnippetService;
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
public class SnippetController {

    @Autowired
    private SnippetMapper snippetMapper;

    @Autowired
    private LikeService likeService;

    @Autowired
    private SnippetService snippetService;

    // 모든 공개 스니펫 목록 페이지
    @GetMapping("/snippets")
    public String showSnippets(
            @RequestParam(value = "sort", defaultValue = "recent") String sort,
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model) {

        Integer currentUserId = AppConfig.getFixedUserId();
        int pageSize = 8; // 페이지당 8개 스니펫 표시
        int offset = (page - 1) * pageSize;

        List<Snippet> snippets;

        // 정렬 방식에 따라 다른 메소드 호출
        if ("popular".equals(sort)) {
            // 인기순 정렬
            snippets = snippetMapper.getPopularSnippetsPaged(offset, pageSize);
        } else {
            // 최신순 정렬
            snippets = snippetMapper.getAllPublicSnippetsPaged(offset, pageSize);
        }

        // 각 스니펫의 좋아요 상태 확인
        Map<Integer, Boolean> likeStatusMap = new HashMap<>();
        for (Snippet snippet : snippets) {
            boolean isLiked = likeService.isLiked(snippet.getSnippetId());
            likeStatusMap.put(snippet.getSnippetId(), isLiked);
        }

        // 페이징 정보 계산
        int totalSnippets = snippetMapper.countAllPublicSnippets();
        int totalPages = (int) Math.ceil((double) totalSnippets / pageSize);

        model.addAttribute("snippets", snippets);
        model.addAttribute("likeStatusMap", likeStatusMap);
        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "snippets";
    }


    @GetMapping("/snippet/{id}")
    public String showSnippetDetail(@PathVariable("id") Integer snippetId, Model model) {
        Integer currentUserId = AppConfig.getFixedUserId();

        // 스니펫 상세 정보 조회
        Snippet snippet = snippetMapper.getSnippetDetailById(snippetId);
        if (snippet == null) {
            return "redirect:/snippets"; // 스니펫이 없으면 목록으로 리다이렉트
        }

        // 스니펫 태그 조회
        List<Tag> tags = snippetService.getTagsBySnippetId(snippetId);

        // 좋아요 상태 확인
        boolean isLiked = likeService.isLiked(snippetId);

        // 실제 좋아요 수 업데이트
        long actualLikeCount = likeService.getLikesCount(snippetId);
        snippet.setLikeCount((int) actualLikeCount);

        model.addAttribute("snippet", snippet);
        model.addAttribute("tags", tags);
        model.addAttribute("isLiked", isLiked);
        model.addAttribute("currentUserId", currentUserId);

        return "snippet-detail"; // snippet-detail.html 템플릿 사용
    }

    // 인기 스니펫 목록 페이지
    @GetMapping("/popular-snippets")
    public String showPopularSnippets(Model model) {
        Integer currentUserId = AppConfig.getFixedUserId();

        // 인기 스니펫 목록 조회 (좋아요 수 기준)
        List<Snippet> snippets = snippetService.getPopularSnippetsJavaSorted(20);

        // 각 스니펫의 좋아요 상태 확인
        Map<Integer, Boolean> likeStatusMap = new HashMap<>();
        for (Snippet snippet : snippets) {
            boolean isLiked = likeService.isLiked(snippet.getSnippetId());
            likeStatusMap.put(snippet.getSnippetId(), isLiked);
        }

        model.addAttribute("snippets", snippets);
        model.addAttribute("likeStatusMap", likeStatusMap);
        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("currentSort", "popular");

        return "snippets";
    }
}