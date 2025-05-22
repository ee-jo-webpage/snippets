package kr.or.kosa.snippets.tag.controller;

import kr.or.kosa.snippets.basic.model.Snippets;
import kr.or.kosa.snippets.basic.service.SnippetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/snippet")
public class TagSnippetController {

    @Autowired
    private SnippetService snippetService;

    @GetMapping
    public ResponseEntity<List<Snippets>> getAllSnippets() {
        List<Snippets> snippets = snippetService.getAllSnippets();
        return ResponseEntity.ok(snippets);
    }
}
