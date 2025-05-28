package kr.or.kosa.snippets.like.controller;

import kr.or.kosa.snippets.like.mapper.LikeSnippetMapper;
import kr.or.kosa.snippets.like.model.Snippet;
import kr.or.kosa.snippets.like.model.LikeTag;
import kr.or.kosa.snippets.like.service.LikeService;
import kr.or.kosa.snippets.like.service.LikeSnippetService;
import kr.or.kosa.snippets.like.service.LikeUserService;
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

    @Autowired
    private LikeUserService likeUserService;  // 추가

    @GetMapping("/snippet/{id}")
    public String showSnippetDetail(@PathVariable("id") Integer snippetId,
                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                    Model model) {

        Snippet snippet = likeSnippetMapper.getSnippetDetailById(snippetId);

        if (snippet == null) {
            return "redirect:/popular-snippets";
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

        // 소유자 nickname 조회 (추가)
        String ownerNickname = likeUserService.getNicknameByUserId(snippet.getUserId());

        model.addAttribute("snippet", snippet);
        model.addAttribute("tags", likeTags);
        model.addAttribute("isLiked", isLiked);
        model.addAttribute("ownerNickname", ownerNickname);  // 추가
        model.addAttribute("currentUserId", userDetails != null ? userDetails.getUserId() : null);
        model.addAttribute("currentUserNickname", userDetails != null ? userDetails.getNickname() : null);
        model.addAttribute("isLoggedIn", userDetails != null);

        return "like/snippet-detail";
    }

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

        int pageSize = 8;
        int offset = (page - 1) * pageSize;
        boolean hasSearchFilter = (type != null && !type.isEmpty()) ||
                (keyword != null && !keyword.isEmpty()) ||
                (minLikes != null && minLikes > 0) ||
                (tagName != null && !tagName.isEmpty());

        List<Snippet> snippets;
        int totalSnippets;

        try {
            // 기존 스니펫 조회 코드...
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
                    snippets = likeSnippetService.filterSnippetsFromTop100(type, keyword, minLikes, tagName);
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

            // 각 스니펫의 좋아요 상태 확인
            Map<Integer, Boolean> likeStatusMap = new HashMap<>();
            if (userDetails != null) {
                for (Snippet snippet : snippets) {
                    boolean isLiked = likeService.isLiked(snippet.getSnippetId(), userDetails.getUserId());
                    likeStatusMap.put(snippet.getSnippetId(), isLiked);
                }
            }

            // 각 스니펫 소유자의 nickname 조회 (추가)
            List<Integer> userIds = snippets.stream()
                    .map(Snippet::getUserId)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Integer, String> nicknameMap = likeUserService.getNicknamesByUserIds(userIds);

            // 스니펫 content 미리보기 조회 (추가)
            Map<Integer, String> contentPreviewMap = likeSnippetService.getSnippetContentPreviews(snippets);

            // 페이징 정보 계산
            int totalPages = (int) Math.ceil((double) totalSnippets / pageSize);

            // 모델에 데이터 추가
            model.addAttribute("snippets", snippets);
            model.addAttribute("likeStatusMap", likeStatusMap);
            model.addAttribute("nicknameMap", nicknameMap);  // 추가
            model.addAttribute("contentPreviewMap", contentPreviewMap);  // 추가
            model.addAttribute("currentUserId", userDetails != null ? userDetails.getUserId() : null);
            model.addAttribute("currentUserNickname", userDetails != null ? userDetails.getNickname() : null);
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
            model.addAttribute("contentPreviewMap", new HashMap<>());  // 추가
            model.addAttribute("snippets", new ArrayList<>());
            model.addAttribute("likeStatusMap", new HashMap<>());
            model.addAttribute("nicknameMap", new HashMap<>());  // 추가
            // 나머지 기본값들...
        }

        return "like/snippets";
    }
}