package kr.or.kosa.snippets.tag.controller;

import jakarta.servlet.http.HttpSession;
import kr.or.kosa.snippets.basic.model.Snippets;
import kr.or.kosa.snippets.basic.service.SnippetService;
import kr.or.kosa.snippets.bookmark.service.BookmarkService;
import kr.or.kosa.snippets.like.mapper.SnippetContentMapper;
import kr.or.kosa.snippets.like.model.LikeTag;
import kr.or.kosa.snippets.like.service.LikeService;
import kr.or.kosa.snippets.like.service.LikeSnippetService;
import kr.or.kosa.snippets.like.service.LikeUserService;
import kr.or.kosa.snippets.tag.model.TagItem;
import kr.or.kosa.snippets.tag.service.TagService;
import kr.or.kosa.snippets.user.service.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tag")
@Slf4j
public class TagController {

    @Autowired
    private TagService tagService;

    @Autowired
    private SnippetService snippetService;

    @Autowired
    BookmarkService bookmarkService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private LikeSnippetService likeSnippetService;

    @Autowired
    private LikeUserService likeUserService;

    @Autowired
    private SnippetContentMapper snippetContentMapper;

    private Long requireLogin(CustomUserDetails userDetails) {
        if (userDetails == null) {
            log.warn("사용자 인증 정보가 없습니다.");
            return null;
        }
        return userDetails.getUserId();
    }

    // 현재 사용자의 모든 태그 조회
    @GetMapping("/my-tags")
    public ResponseEntity<List<TagItem>> getMyTags(@AuthenticationPrincipal CustomUserDetails userDetails) {

//        Long userId = (Long) session.getAttribute("userId");
        Long userId = requireLogin(userDetails);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<TagItem> tags = tagService.getTagsByUserId(userId);
        return ResponseEntity.ok(tags != null ? tags : new ArrayList<>());
    }

    // 태그 검색 API 추가
    @GetMapping("/search")
    public ResponseEntity<List<TagItem>> searchTags(@RequestParam("query") String keyword,
                                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = requireLogin(userDetails);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        List<TagItem> tags = tagService.searchTagsByKeyword(userId, keyword.trim());
        return ResponseEntity.ok(tags != null ? tags : new ArrayList<>());
    }

    // 사용자 Id로 모든 태그 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TagItem>> getTagsByUserId(@PathVariable("userId") Long userId) {
        List<TagItem> tagList = tagService.getTagsByUserId(userId);
        return ResponseEntity.ok(tagList != null ? tagList : new ArrayList<>());
    }

    // 사용자 Id와 태그이름으로 태그 조회
    @GetMapping("/user/{userId}/name/{name}")
    public ResponseEntity<TagItem> getTagByUserIdAndName(@PathVariable("userId") Long userId,
                                                         @PathVariable("name") String name) {
        TagItem tag = tagService.getTagByUserIdAndName(userId, name);
        if (tag != null) {
            return ResponseEntity.ok(tag);
        }
        return ResponseEntity.notFound().build();
    }

    // 새 태그 생성 (현재 사용자)
    @PostMapping
    public ResponseEntity<Map<String, Object>> createTag(@RequestBody Map<String, String> request,
                                                         @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = requireLogin(userDetails);
//        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String tagName = request.get("name");
        if (tagName == null || tagName.trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "태그명을 입력해주세요");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            TagItem createdTag = tagService.createTag(userId, tagName.trim());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("tag", createdTag);
            response.put("message", "태그가 성공적으로 생성되었습니다");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);

        } catch (Exception e) {
            log.error("태그 생성 중 오류 발생", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "태그 생성에 실패했습니다");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // 스니펫에 태그 추가 (권한 확인 포함)
    @PostMapping("/snippet/{snippetId}/tag/{tagId}")
    public ResponseEntity<Map<String, Object>> addTagToSnippet(@PathVariable Long snippetId,
                                                               @PathVariable Long tagId,
                                                               @AuthenticationPrincipal CustomUserDetails userDetails) {
//        Long userId = (Long) session.getAttribute("userId");
        Long userId = requireLogin(userDetails);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 태그 소유권 확인
        if (!tagService.isTagOwnedByUser(tagId, userId)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "해당 태그에 대한 권한이 없습니다");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        boolean added = tagService.addTagToSnippet(snippetId, tagId);
        Map<String, Object> response = new HashMap<>();

        if (added) {
            response.put("success", true);
            response.put("message", "태그가 스니펫에 추가되었습니다");
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "이미 연결된 태그이거나 추가에 실패했습니다");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
    }

    // 특정 스니펫의 모든 태그 조회
    @GetMapping("/snippet/{snippetId}")
    public ResponseEntity<List<TagItem>> getTagsBySnippetId(@PathVariable Long snippetId) {
        if (snippetId == null) {
            return ResponseEntity.badRequest().build();
        }

        log.debug("스니펫 ID: {} 태그 조회 요청", snippetId);
        List<TagItem> tags = tagService.getTagsBySnippetId(snippetId);
        log.debug("스니펫 ID: {}, 태그 수: {}", snippetId, tags != null ? tags.size() : "null");

        return ResponseEntity.ok(tags != null ? tags : new ArrayList<>());
    }

    // 스니펫에서 태그 제거
    @DeleteMapping("/snippet/{snippetId}/tag/{tagId}")
    public ResponseEntity<Map<String, Object>> removeTagFromSnippet(@PathVariable Long snippetId,
                                                                    @PathVariable Long tagId,
                                                                    @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = requireLogin(userDetails);
//        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean removed = tagService.removeTagFromSnippet(snippetId, tagId);
        Map<String, Object> response = new HashMap<>();

        if (removed) {
            response.put("success", true);
            response.put("message", "태그가 스니펫에서 제거되었습니다");
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "태그 제거에 실패했습니다");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // 태그 삭제 (사용자 권한 확인)
    @DeleteMapping("/{tagId}")
    public ResponseEntity<Map<String, Object>> deleteTag(@PathVariable Long tagId,
                                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = requireLogin(userDetails);
//        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean deleted = tagService.deleteTag(tagId, userId);
        Map<String, Object> response = new HashMap<>();

        if (deleted) {
            response.put("success", true);
            response.put("message", "태그가 삭제되었습니다");
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "태그 삭제에 실패했습니다. 권한을 확인해주세요");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
    }

    // 특정 태그의 스니펫 조회
    @GetMapping("/{tagId}/snippets")
    public ResponseEntity<List<Snippets>> getSnippetsByTagId(@PathVariable Long tagId,
                                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = requireLogin(userDetails);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 태그 소유권 확인
        if (!tagService.isTagOwnedByUser(tagId, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Snippets> snippets = tagService.getSnippetsByTagId(tagId);
        return ResponseEntity.ok(snippets != null ? snippets : new ArrayList<>());
    }

    @GetMapping("/snippet/{snippetId}/detail")
    public ResponseEntity<?> getBookmarkedSnippetDetail(@PathVariable Long snippetId,
                                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long userId = requireLogin(userDetails);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }


            Snippets snippet = bookmarkService.getSnippetById(snippetId);
            if (snippet == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "스니펫을 찾을 수 없습니다."));
            }

            Map<String, Object> result = new HashMap<>();


            // 기본 스니펫 정보
            result.put("snippet", snippet);

            // 스니펫 태그 조회
            List<LikeTag> tags = likeSnippetService.getTagsBySnippetId(snippetId);
            result.put("tags", tags);

            // 좋아요 상태 확인
            boolean isLiked = likeService.isLiked(snippetId, userId);
            result.put("isLiked", isLiked);

            // 실제 좋아요 수 업데이트
            long actualLikeCount = likeService.getLikesCount(snippetId);
            result.put("actualLikeCount", actualLikeCount);

            // 소유자 nickname 조회
            String ownerNickname = likeUserService.getNicknameByUserId(snippet.getUserId());
            result.put("ownerNickname", ownerNickname);

            // 스니펫 타입별 실제 content 조회
            Object snippetContent = null;
            try {
                switch (snippet.getType().toString().toUpperCase()) {
                    case "CODE":
                        snippetContent = snippetContentMapper.getSnippetCodeById(snippetId);
                        break;
                    case "TEXT":
                        snippetContent = snippetContentMapper.getSnippetTextById(snippetId);
                        break;
                    case "IMG":
                        snippetContent = snippetContentMapper.getSnippetImageById(snippetId);
                        break;
                }
            } catch (Exception e) {
                log.error("스니펫 content 조회 실패 - 스니펫 ID: {}, 오류: {}", snippetId, e.getMessage());
            }
            result.put("snippetContent", snippetContent);

            // 북마크된 스니펫인지 확인
            if (bookmarkService.isBookmarked(userId, snippetId)) {
                result.put("isBookmarked", true);
            }


            // 현재 사용자 정보
            result.put("currentUserId", userId);
            result.put("currentUserNickname", userDetails.getNickname());
            result.put("isLoggedIn", true);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", result
            ));

        } catch (Exception e) {
            log.error("스니펫 상세정보 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "스니펫 상세정보 조회 중 오류가 발생했습니다."));
        }
    }
}