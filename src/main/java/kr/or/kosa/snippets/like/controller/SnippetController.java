package kr.or.kosa.snippets.like.controller;

import kr.or.kosa.snippets.config.AppConfig;
import kr.or.kosa.snippets.like.mapper.SnippetMapper;
import kr.or.kosa.snippets.like.model.Snippet;
import kr.or.kosa.snippets.like.model.Tag;
import kr.or.kosa.snippets.like.service.LikeService;
import kr.or.kosa.snippets.like.service.SnippetService;
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

    // 모든 공개 스니펫 목록 페이지(검색 필터 포함)
    @GetMapping("/snippets")
    public String showSnippets(
            @RequestParam(value = "sort", defaultValue = "recent") String sort,
            // URL 파라미터 "sort"를 받음. 값이 없으면 "recent"가 기본값
            // 예: /snippets?sort=popular
            @RequestParam(value = "page", defaultValue = "1") int page,
            // URL 파라미터 "page"를 받음. 값이 없으면 1이 기본값
            // 예: /snippets?page=2
            @RequestParam(value = "type", required = false) String type,
            // 검색 필터: 스니펫 유형 (code, text, img)
            @RequestParam(value = "keyword", required = false) String keyword,
            // 검색 필터: 키워드 (메모에서 검색)
            @RequestParam(value = "minLikes", required = false) Integer minLikes,
            // 검색 필터: 최소 좋아요 수
            @RequestParam(value = "tagName", required = false) String tagName,
            // 검색 필터: 태그명
            Model model) {
        // 뷰에 데이터를 전달하기 위한 Model 객체

        Integer currentUserId = AppConfig.getFixedUserId();
        // 현재 로그인한 사용자 ID를 가져옴 (현재는 고정값 2)

        int pageSize = 8; // 페이지당 8개 스니펫 표시
        // 한 페이지에 보여줄 스니펫 개수를 8개로 설정

        int offset = (page - 1) * pageSize;
        // 데이터베이스 쿼리에서 건너뛸 레코드 수 계산
        // 1페이지: (1-1)*8 = 0, 2페이지: (2-1)*8 = 8

        List<Snippet> snippets;
        int totalSnippets;

        // 검색 필터가 있는 경우
        if (hasSearchFilters(type, keyword, minLikes, tagName)) {
            // 검색 기능을 사용하여 필터링된 스니펫 조회
            snippets = snippetService.searchSnippets(type, keyword, minLikes, tagName);

            // 검색 결과를 정렬 방식에 따라 정렬
            if ("popular".equals(sort)) {
                // 인기순 정렬: 좋아요 수 내림차순
                snippets.sort((s1, s2) -> Integer.compare(s2.getLikeCount(), s1.getLikeCount()));
            } else {
                // 최신순 정렬: 생성일 내림차순
                snippets.sort((s1, s2) -> s2.getCreatedAt().compareTo(s1.getCreatedAt()));
            }

            // 페이징 처리 (메모리에서 처리)
            totalSnippets = snippets.size();
            int fromIndex = Math.min(offset, snippets.size());
            int toIndex = Math.min(offset + pageSize, snippets.size());
            snippets = snippets.subList(fromIndex, toIndex);

        } else {
            // 검색 필터가 없는 경우 - 기존 로직
            // 정렬 방식에 따라 다른 메소드 호출
            if ("popular".equals(sort)) {
                // 인기순 정렬
                snippets = snippetMapper.getPopularSnippetsPaged(offset, pageSize);
                // 좋아요 수가 많은 순서대로 정렬된 스니펫들을 페이징 처리해서 가져옴
            } else {
                // 최신순 정렬
                snippets = snippetMapper.getAllPublicSnippetsPaged(offset, pageSize);
                // 생성일이 최신인 순서대로 정렬된 스니펫들을 페이징 처리해서 가져옴
            }
            totalSnippets = snippetMapper.countAllPublicSnippets();
        }

        // 각 스니펫의 좋아요 상태 확인
        Map<Integer, Boolean> likeStatusMap = new HashMap<>();
        // 스니펫 ID를 키로, 좋아요 상태를 값으로 하는 맵 생성

        for (Snippet snippet : snippets) {
            boolean isLiked = likeService.isLiked(snippet.getSnippetId());
            // 현재 사용자가 이 스니펫에 좋아요를 눌렀는지 확인

            likeStatusMap.put(snippet.getSnippetId(), isLiked);
            // 맵에 스니펫 ID와 좋아요 상태를 저장
        }

        // 페이징 정보 계산
        int totalPages = (int) Math.ceil((double) totalSnippets / pageSize);
        // 전체 페이지 수 계산 (올림 처리)
        // 예: 총 18개 스니펫, 페이지당 8개 → 3페이지

        // 검색용 태그 목록 조회 (검색 폼에서 사용)
        List<Tag> allTags = snippetService.getAllTags();

        //뷰에 전달 - 기존 데이터
        model.addAttribute("snippets", snippets);
        model.addAttribute("likeStatusMap", likeStatusMap);
        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalSnippets", totalSnippets);

        // 검색 관련 데이터 추가
        model.addAttribute("searchType", type);
        model.addAttribute("searchKeyword", keyword);
        model.addAttribute("searchMinLikes", minLikes);
        model.addAttribute("searchTagName", tagName);
        model.addAttribute("allTags", allTags);
        model.addAttribute("hasSearchFilter", hasSearchFilters(type, keyword, minLikes, tagName));

        return "like/snippets";
    }

    // 검색 필터가 있는지 확인하는 헬퍼 메서드
    private boolean hasSearchFilters(String type, String keyword, Integer minLikes, String tagName) {
        return (type != null && !type.trim().isEmpty()) ||
                (keyword != null && !keyword.trim().isEmpty()) ||
                (minLikes != null && minLikes > 0) ||
                (tagName != null && !tagName.trim().isEmpty());
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

        return "like/snippet-detail"; // snippet-detail.html 템플릿 사용
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

        return "like/snippets";
    }
}