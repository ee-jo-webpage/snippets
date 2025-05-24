package kr.or.kosa.snippets.tag.controller;

import kr.or.kosa.snippets.basic.service.SnippetService;
import kr.or.kosa.snippets.tag.model.TagItem;
import kr.or.kosa.snippets.tag.service.TagService;
import kr.or.kosa.snippets.user.service.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    // private 헬퍼 메서드
    private Long requireLogin(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다");
        }
        return userDetails.getUserId();
    }

    // 현재 사용자의 모든 태그 조회
    @GetMapping("/my-tags")
    public ResponseEntity<List<TagItem>> getMyTags(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = requireLogin(userDetails);

        log.info("사용자 {}의 태그 조회", userId);
        List<TagItem> tags = tagService.getTagsByUserId(userId);
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
        Long userId = requireLogin(userDetails);

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

    // 디버깅용 - 현재 인증 정보 확인
    @GetMapping("/auth-check")
    public ResponseEntity<Map<String, Object>> checkAuth(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        if (userDetails != null) {
            response.put("authenticated", true);
            response.put("userId", userDetails.getUserId());
            response.put("username", userDetails.getUsername());
            response.put("authorities", userDetails.getAuthorities());
        } else {
            response.put("authenticated", false);
            response.put("message", "로그인되지 않은 사용자입니다");
        }

        log.info("인증 확인: {}", response);

        return ResponseEntity.ok(response);
    }
}