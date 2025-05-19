package kr.or.kosa.snippets.snippetExt.controller;

import kr.or.kosa.snippets.snippetExt.model.SnippetExtCreate;
import kr.or.kosa.snippets.snippetExt.model.SnippetExtUpdate;
import kr.or.kosa.snippets.snippetExt.service.SnippetService;
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

    private final SnippetService snippetService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> save(@RequestBody SnippetExtCreate snippet) {
        Long snippetId = snippetService.save(snippet);
        Map<String, Object> response = Map.of("snippetId", snippetId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateSnippet(@PathVariable Long id, @RequestBody SnippetExtUpdate snippetUpdate) {
        snippetUpdate.setSnippetId(id);
        snippetService.updateSnippet(snippetUpdate);
        return ResponseEntity.ok().build();
    }


}
