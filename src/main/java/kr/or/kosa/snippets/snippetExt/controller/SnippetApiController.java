package kr.or.kosa.snippets.snippetExt.controller;

import kr.or.kosa.snippets.snippetExt.model.ColorExt;
import kr.or.kosa.snippets.snippetExt.model.SnippetExtCreate;
import kr.or.kosa.snippets.snippetExt.model.SnippetExtUpdate;
import kr.or.kosa.snippets.snippetExt.service.SnippetExtService;
import kr.or.kosa.snippets.user.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/snippets")
@RequiredArgsConstructor
@Slf4j
public class SnippetApiController {

    private final SnippetExtService snippetExtService;

    // 인증 체크 메서드
    private Long requireLogin(CustomUserDetails details) {
        if (details == null) {
            throw new AuthenticationCredentialsNotFoundException("로그인이 필요합니다.");
        }
        return details.getUserId();
    }

    @PostMapping
    public ResponseEntity<?> save(
        @RequestBody SnippetExtCreate snippet,
        @AuthenticationPrincipal CustomUserDetails details
    ) {
        snippet.setUserId(requireLogin(details));
        Long snippetId = snippetExtService.save(snippet);
        return ResponseEntity.ok(Map.of("snippetId", snippetId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateSnippet(
        @PathVariable Long id,
        @RequestBody SnippetExtUpdate snippetUpdate,
        @AuthenticationPrincipal CustomUserDetails details
    ) {
        snippetUpdate.setSnippetId(id);
        snippetExtService.updateSnippet(snippetUpdate, requireLogin(details));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSnippet(
        @PathVariable Long id,
        @AuthenticationPrincipal CustomUserDetails details
    ) {
        snippetExtService.deleteSnippet(id, requireLogin(details));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/color")
    public ResponseEntity<?> getColorByUserId(
        @AuthenticationPrincipal CustomUserDetails details
    ) {
        Long userId = requireLogin(details);
        List<ColorExt> colorList = snippetExtService.getColorsByUserId(userId);
        return ResponseEntity.ok(colorList);
    }
}
