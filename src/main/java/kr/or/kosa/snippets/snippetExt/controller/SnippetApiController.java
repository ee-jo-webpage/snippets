package kr.or.kosa.snippets.snippetExt.controller;

import kr.or.kosa.snippets.snippetExt.model.SnippetExtCreate;
import kr.or.kosa.snippets.snippetExt.model.SnippetExtUpdate;
import kr.or.kosa.snippets.snippetExt.service.SnippetExtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/snippets")
@RequiredArgsConstructor
@Slf4j
public class SnippetApiController {

    private final SnippetExtService snippetExtService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> save(@RequestBody SnippetExtCreate snippet) {
        Long snippetId = snippetExtService.save(snippet);
        Map<String, Object> response = Map.of("snippetId", snippetId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateSnippet(@PathVariable Long id, @RequestBody SnippetExtUpdate snippetUpdate) {
        snippetUpdate.setSnippetId(id);
        snippetExtService.updateSnippet(snippetUpdate);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSnippet(@PathVariable Long id) {
        log.warn(String.valueOf(id));
        snippetExtService.deleteSnippet(id);
        return ResponseEntity.ok().build();
    }



}
