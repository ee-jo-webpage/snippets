package kr.or.kosa.snippets.like.controller;

import kr.or.kosa.snippets.like.mapper.LikeSnippetMapper;
import kr.or.kosa.snippets.like.model.Snippet;
import kr.or.kosa.snippets.like.model.LikeTag;
import kr.or.kosa.snippets.like.service.LikeService;
import kr.or.kosa.snippets.like.service.LikeSnippetService;
import kr.or.kosa.snippets.user.service.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "스니펫을 찾을 수 없습니다.");
        }

        // 비공개 스니펫 접근 권한 체크 - 보안 강화
        if (!snippet.isVisibility()) { // visibility가 false(0)인 경우
            if (userDetails == null) {
                // 비로그인 사용자는 로그인 페이지로 리다이렉트
                return "redirect:/login?message=login_required";
            }

            if (!userDetails.getUserId().equals((long) snippet.getUserId())) {
                // 작성자가 아닌 경우 접근 거부
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "이 스니펫에 접근할 권한이 없습니다.");
            }
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

        // 보안 로그 추가
        String userInfo = userDetails != null ?
                "User ID: " + userDetails.getUserId() + ", Nickname: " + userDetails.getNickname() :
                "Anonymous";
        System.out.println("인기 스니펫 페이지 접근 - " + userInfo);

        int pageSize = 8;
        int offset = (page - 1) * pageSize;
        boolean hasSearchFilter = (type != null && !type.isEmpty()) ||
                (keyword != null && !keyword.isEmpty()) ||
                (minLikes != null && minLikes > 0) ||
                (tagName != null && !tagName.isEmpty());

        List<Snippet> snippets;
        int totalSnippets;

        if (hasSearchFilter) {
            if ("db".equals(searchMode)) {
                snippets = likeSnippetService.searchSnippets(type, keyword, minLikes, tagName);
                totalSnippets = snippets.size();

                int fromIndex = Math.min(offset, snippets.size());
                int toIndex = Math.min(offset + pageSize, snippets.size());
                if (fromIndex < toIndex) {
                    snippets = snippets.subList(fromIndex, toIndex);
                } else {
                    snippets = new ArrayList<>();
                }
            } else {
                List<Snippet> allPopularSnippets = likeSnippetService.getTop100PopularSnippets(100);

                snippets = allPopularSnippets.stream()
                        .filter(s -> type == null || type.isEmpty() || s.getType().equals(type))
                        .filter(s -> keyword == null || keyword.isEmpty() ||
                                (s.getMemo() != null && s.getMemo().toLowerCase().contains(keyword.toLowerCase())))
                        .filter(s -> minLikes == null || s.getLikeCount() >= minLikes)
                        .collect(Collectors.toList());

                if (tagName != null && !tagName.isEmpty()) {
                    snippets = snippets.stream()
                            .filter(s -> {
                                List<LikeTag> snippetTags = likeSnippetService.getTagsBySnippetId(s.getSnippetId());
                                return snippetTags.stream()
                                        .anyMatch(tag -> tag.getName().equalsIgnoreCase(tagName));
                            })
                            .collect(Collectors.toList());
                }

                totalSnippets = snippets.size();

                int fromIndex = Math.min(offset, snippets.size());
                int toIndex = Math.min(offset + pageSize, snippets.size());
                if (fromIndex < toIndex) {
                    snippets = snippets.subList(fromIndex, toIndex);
                } else {
                    snippets = new ArrayList<>();
                }
            }
        } else {
            snippets = likeSnippetService.getPopularSnippetsPagedFromView(offset, pageSize);
            totalSnippets = likeSnippetService.countPopularSnippetsFromView();
        }

        // 각 스니펫의 좋아요 상태 확인 (로그인한 경우만)
        Map<Integer, Boolean> likeStatusMap = new HashMap<>();
        if (userDetails != null) {
            for (Snippet snippet : snippets) {
                boolean isLiked = likeService.isLiked(snippet.getSnippetId(), userDetails.getUserId());
                likeStatusMap.put(snippet.getSnippetId(), isLiked);
            }
        }

        int totalPages = (int) Math.ceil((double) totalSnippets / pageSize);

        // 모델에 데이터 추가
        model.addAttribute("currentUserId", userDetails != null ? userDetails.getUserId() : null);
        model.addAttribute("currentUserNickname", userDetails != null ? userDetails.getNickname() : null);
        model.addAttribute("isLoggedIn", userDetails != null);
        model.addAttribute("snippets", snippets);
        model.addAttribute("likeStatusMap", likeStatusMap);
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

        return "like/snippets";
    }
}