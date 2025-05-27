package kr.or.kosa.snippets.community.controller;

import kr.or.kosa.snippets.community.model.PostDraft;
import kr.or.kosa.snippets.community.service.BoardService;
import kr.or.kosa.snippets.user.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/community/draft")
@RequiredArgsConstructor
public class DraftRestController {
    private final BoardService boardService;

    // 임시저장 목록
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getDraftList(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // 로그인 체크
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            Long userId = userDetails.getUserId();
            List<PostDraft> drafts = boardService.getDraftsByUserId(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("drafts", drafts);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("임시저장 목록 조회 오류: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "임시저장 목록을 불러올 수 없습니다."));
        }
    }

    // 임시저장 조회
    @GetMapping("/{draftId}")
    public ResponseEntity<Map<String, Object>> getDraft(@PathVariable Integer draftId,
                                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // 로그인 체크
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            Long userId = userDetails.getUserId();
            PostDraft draft = boardService.getDraftById(draftId);

            if (draft == null || !draft.getUserId().equals(userId)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "임시저장을 찾을 수 없습니다."));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("draft", draft);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("임시저장 조회 오류: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "임시저장을 불러올 수 없습니다."));
        }
    }

    // 임시저장
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveDraft(@RequestBody PostDraft draft,
                                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // 로그인 체크
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            Long userId = userDetails.getUserId();
            draft.setUserId(userId);
            Integer draftId = boardService.saveDraft(draft);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("draftId", draftId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("임시저장 오류: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "임시저장에 실패했습니다."));
        }
    }

    // 자동저장
    @PostMapping("/auto-save")
    public ResponseEntity<Map<String, Object>> autoSaveDraft(@RequestBody PostDraft draft,
                                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // 로그인 체크
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            Long userId = userDetails.getUserId();
            draft.setUserId(userId);
            Integer draftId = boardService.saveDraft(draft);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("draftId", draftId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("자동저장 오류: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "자동저장에 실패했습니다."));
        }
    }

    // 임시저장 삭제
    @DeleteMapping("/{draftId}")
    public ResponseEntity<Map<String, Object>> deleteDraft(@PathVariable Integer draftId,
                                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // 로그인 체크
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            Long userId = userDetails.getUserId();
            PostDraft draft = boardService.getDraftById(draftId);

            if (draft == null || !draft.getUserId().equals(userId)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "임시저장을 찾을 수 없습니다."));
            }

            boardService.deleteDraft(draftId);

            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            System.out.println("임시저장 삭제 오류: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "임시저장 삭제에 실패했습니다."));
        }
    }
}