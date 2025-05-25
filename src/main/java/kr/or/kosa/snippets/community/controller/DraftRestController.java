package kr.or.kosa.snippets.community.controller;

import kr.or.kosa.snippets.community.model.PostDraft;
import kr.or.kosa.snippets.community.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/board/draft")
@RequiredArgsConstructor
public class DraftRestController {
    private final BoardService boardService;

    // 임시저장 목록
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getDraftList(@AuthenticationPrincipal UserDetails userDetails) {
        // Long userId = ((CustomUserDetails) userDetails).getUserId();
        Long userId = 1L; // 임시

        List<PostDraft> drafts = boardService.getDraftsByUserId(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("drafts", drafts);

        return ResponseEntity.ok(response);
    }

    // 임시저장 조회
    @GetMapping("/{draftId}")
    public ResponseEntity<Map<String, Object>> getDraft(@PathVariable Integer draftId,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        // Long userId = ((CustomUserDetails) userDetails).getUserId();
        Long userId = 1L; // 임시

        PostDraft draft = boardService.getDraftById(draftId);

        if (draft == null || !draft.getUserId().equals(userId)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "임시저장을 찾을 수 없습니다."));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("draft", draft);

        return ResponseEntity.ok(response);
    }

    // 임시저장
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveDraft(@RequestBody PostDraft draft,
                                                         @AuthenticationPrincipal UserDetails userDetails) {
        // Long userId = ((CustomUserDetails) userDetails).getUserId();
        Long userId = 1L; // 임시

        draft.setUserId(userId);
        Integer draftId = boardService.saveDraft(draft);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("draftId", draftId);

        return ResponseEntity.ok(response);
    }

    // 자동저장
    @PostMapping("/auto-save")
    public ResponseEntity<Map<String, Object>> autoSaveDraft(@RequestBody PostDraft draft,
                                                             @AuthenticationPrincipal UserDetails userDetails) {
        // Long userId = ((CustomUserDetails) userDetails).getUserId();
        Long userId = 1L; // 임시

        draft.setUserId(userId);
        Integer draftId = boardService.saveDraft(draft);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("draftId", draftId);

        return ResponseEntity.ok(response);
    }

    // 임시저장 삭제
    @DeleteMapping("/{draftId}")
    public ResponseEntity<Map<String, Object>> deleteDraft(@PathVariable Integer draftId,
                                                           @AuthenticationPrincipal UserDetails userDetails) {
        // Long userId = ((CustomUserDetails) userDetails).getUserId();
        Long userId = 1L; // 임시

        PostDraft draft = boardService.getDraftById(draftId);

        if (draft == null || !draft.getUserId().equals(userId)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "임시저장을 찾을 수 없습니다."));
        }

        boardService.deleteDraft(draftId);

        return ResponseEntity.ok(Map.of("success", true));
    }
}