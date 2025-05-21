package kr.or.kosa.snippets.tag.controller;

import kr.or.kosa.snippets.basic.service.SnippetService;
import kr.or.kosa.snippets.tag.model.TagItem;
import kr.or.kosa.snippets.tag.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tag")
public class TagController {

    @Autowired
    private TagService tagService;

    @Autowired
    private SnippetService snippetService;

    //모든 태그 조회
    @GetMapping
    public ResponseEntity<List<TagItem>> getAllTags() {
        List<TagItem> tags = tagService.selectAllTags();

        return ResponseEntity.ok(tags);
    }



}
