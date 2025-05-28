package kr.or.kosa.snippets.like.controller;

import kr.or.kosa.snippets.like.mapper.LikeSnippetMapper;
import kr.or.kosa.snippets.like.model.Snippet;
import kr.or.kosa.snippets.like.model.LikeTag;
import kr.or.kosa.snippets.like.service.LikeService;
import kr.or.kosa.snippets.like.service.LikeSnippetService;
import kr.or.kosa.snippets.user.service.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Controller
public class LikeSnippetController {

    @Autowired
    private LikeSnippetMapper likeSnippetMapper;

    @Autowired
    private LikeService likeService;

    @Autowired
    private LikeSnippetService likeSnippetService;

    @GetMapping("/snippet/{id}")
    public String showSnippetDetail(@PathVariable("id") Integer snippetId,
                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                    Model model) {

        // 스니펫 상세 정보 조회
        Snippet snippet = likeSnippetMapper.getSnippetDetailById(snippetId);

        if (snippet == null) {
            return "redirect:/popular-snippets"; // 스니펫이 없으면 인기 스니펫 페이지로 리다이렉트
        }

        // 스니펫 태그 조회
        List<LikeTag> likeTags = likeSnippetService.getTagsBySnippetId(snippetId);

        // 좋아요 상태 확인 (로그인한 경우만)
        boolean isLiked = false;
        if (userDetails != null) {
            isLiked = likeService.isLiked(snippetId, userDetails.getUserId());
        }

        // 실제 좋아요 수 업데이트
        long actualLikeCount = likeService.getLikesCount(snippetId);
        snippet.setLikeCount((int) actualLikeCount);

        model.addAttribute("snippet", snippet);
        model.addAttribute("tags", likeTags);
        model.addAttribute("isLiked", isLiked);
        model.addAttribute("currentUserId", userDetails != null ? userDetails.getUserId() : null);
        model.addAttribute("currentUserNickname", userDetails != null ? userDetails.getNickname() : null);
        model.addAttribute("isLoggedIn", userDetails != null);

        return "like/snippet-detail";
    }

    // 인기 스니펫 목록 페이지 (상위 100개 제한, 페이징 처리)
    @GetMapping("/popular-snippets")
    public String showPopularSnippets(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "minLikes", required = false) Integer minLikes,
            @RequestParam(value = "tagName", required = false) String tagName,
            @RequestParam(value = "searchMode", required = false, defaultValue = "view") String searchMode,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        int pageSize = 8; // 페이지당 8개 스니펫 표시
        int offset = (page - 1) * pageSize;
        boolean hasSearchFilter = (type != null && !type.isEmpty()) ||
                (keyword != null && !keyword.isEmpty()) ||
                (minLikes != null && minLikes > 0) ||
                (tagName != null && !tagName.isEmpty());

        List<Snippet> snippets;
        int totalSnippets;

        try {
            // 검색 모드에 따라 처리 방식 다르게 적용
            if (hasSearchFilter) {
                if ("db".equals(searchMode)) {
                    // DB 검색 모드: 전체 데이터베이스에서 직접 검색 (상위 100개로 제한)
                    snippets = likeSnippetService.searchSnippets(type, keyword, minLikes, tagName);
                    System.out.println("DB 검색 모드 - 검색 결과 수: " + snippets.size()); // 디버깅 로그
                    totalSnippets = snippets.size();

                    // 페이징 처리
                    int fromIndex = Math.min(offset, snippets.size());
                    int toIndex = Math.min(offset + pageSize, snippets.size());
                    if (fromIndex < toIndex) {
                        snippets = snippets.subList(fromIndex, toIndex);
                    } else {
                        snippets = new ArrayList<>(); // 빈 목록 반환
                    }
                } else {
                    // 뷰 검색 모드 (기본값): 상위 100개 내에서 필터링 (인메모리 방식)
                    snippets = likeSnippetService.filterSnippetsFromTop100(type, keyword, minLikes, tagName);
                    totalSnippets = snippets.size();

                    // 페이징 처리
                    int fromIndex = Math.min(offset, snippets.size());
                    int toIndex = Math.min(offset + pageSize, snippets.size());
                    if (fromIndex < toIndex) {
                        snippets = snippets.subList(fromIndex, toIndex);
                    } else {
                        snippets = new ArrayList<>(); // 빈 목록 반환
                    }
                }
            } else {
                // 검색 필터가 없는 경우 기본 인기 스니펫 목록 표시
                snippets = likeSnippetService.getPopularSnippetsPagedFromView(offset, pageSize);
                totalSnippets = likeSnippetService.countPopularSnippetsFromView(); // 최대 100개
            }

            // 각 스니펫의 좋아요 상태 확인 (로그인한 경우만)
            Map<Integer, Boolean> likeStatusMap = new HashMap<>();
            if (userDetails != null) {
                for (Snippet snippet : snippets) {
                    boolean isLiked = likeService.isLiked(snippet.getSnippetId(), userDetails.getUserId());
                    likeStatusMap.put(snippet.getSnippetId(), isLiked);
                }
            }

            // 페이징 정보 계산
            int totalPages = (int) Math.ceil((double) totalSnippets / pageSize);

            // 현재 사용자 정보 추가
            model.addAttribute("currentUserId", userDetails != null ? userDetails.getUserId() : null);
            model.addAttribute("currentUserNickname", userDetails != null ? userDetails.getNickname() : null);
            model.addAttribute("isLoggedIn", userDetails != null);

            model.addAttribute("snippets", snippets);
            model.addAttribute("likeStatusMap", likeStatusMap);
            model.addAttribute("currentUserId", userDetails != null ? userDetails.getUserId() : null);
            model.addAttribute("isLoggedIn", userDetails != null);
            model.addAttribute("currentSort", "popular");
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalSnippets", totalSnippets);
            model.addAttribute("searchType", type);
            model.addAttribute("searchKeyword", keyword);
            model.addAttribute("searchMinLikes", minLikes);
            model.addAttribute("searchTagName", tagName);
            model.addAttribute("searchMode", searchMode);
            model.addAttribute("hasSearchFilter", hasSearchFilter);

        } catch (Exception e) {
            System.err.println("인기 스니펫 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();

            // 오류 발생 시 기본값 설정
            model.addAttribute("snippets", new ArrayList<>());
            model.addAttribute("likeStatusMap", new HashMap<>());
            model.addAttribute("currentUserId", userDetails != null ? userDetails.getUserId() : null);
            model.addAttribute("currentUserNickname", userDetails != null ? userDetails.getNickname() : null);
            model.addAttribute("isLoggedIn", userDetails != null);
            model.addAttribute("currentSort", "popular");
            model.addAttribute("currentPage", 1);
            model.addAttribute("totalPages", 0);
            model.addAttribute("totalSnippets", 0);
            model.addAttribute("searchType", type);
            model.addAttribute("searchKeyword", keyword);
            model.addAttribute("searchMinLikes", minLikes);
            model.addAttribute("searchTagName", tagName);
            model.addAttribute("searchMode", searchMode);
            model.addAttribute("hasSearchFilter", hasSearchFilter);
            model.addAttribute("error", "스니펫을 불러오는 중 오류가 발생했습니다.");
        }

        return "like/snippets";
    }
}