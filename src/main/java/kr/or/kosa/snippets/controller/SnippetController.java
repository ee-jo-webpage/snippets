package kr.or.kosa.snippets.controller;

import kr.or.kosa.snippets.model.Snippet;
import kr.or.kosa.snippets.service.SnippetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/snippet")
public class SnippetController {

    @Autowired
    private SnippetService snippetService;

    @GetMapping
    public ResponseEntity<List<Snippet>> getAllSnippets() {
        List<Snippet> snippets = snippetService.getAllSnippets();
        return ResponseEntity.ok(snippets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Snippet> getSnippetById(@PathVariable Long id) {
        Snippet snippet = snippetService.getSnippetById(id);
        if (snippet != null) {
            return ResponseEntity.ok(snippet);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Snippet> createSnippet(@RequestBody Snippet snippet) {
        Snippet createdSnippet = snippetService.createSnippet(snippet);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSnippet);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Snippet> updateSnippet(@PathVariable Long id, @RequestBody Snippet snippet) {
        snippet.setId(id);
        snippetService.updateSnippet(snippet);
        return ResponseEntity.ok(snippet);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSnippet(@PathVariable Long id) {
        snippetService.deleteSnippet(id);
        return ResponseEntity.noContent().build();
    }
}