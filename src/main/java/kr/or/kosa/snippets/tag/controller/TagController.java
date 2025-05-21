package kr.or.kosa.snippets.tag.controller;

import kr.or.kosa.snippets.basic.service.SnippetService;
import kr.or.kosa.snippets.tag.model.TagItem;
import kr.or.kosa.snippets.tag.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
        List<TagItem> tags = tagService.getAllTags();

        return ResponseEntity.ok(tags);
    }

    //태그 이름으로 태그 조회
    @GetMapping("/name/{name}")
    public ResponseEntity<TagItem> getTagByName(@PathVariable("name") String name) {

        String findingName = name.toLowerCase();
        TagItem tag = tagService.getTagByName(name);
        String comparedName = tag.getName().toLowerCase();

        if (findingName == comparedName) {
            return ResponseEntity.ok(tag);
        }

        return ResponseEntity.notFound().build();
    }

    //새 태그 생성
    @PostMapping
    public ResponseEntity<TagItem> createTag(@RequestBody TagItem tag) {

        //태그명 중복확인
        TagItem existingTag = tagService.getTagByName(tag.getName());
        if (existingTag != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(existingTag);
        }

        TagItem createdTag = tagService.createTag(tag.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdTag);
    }



}
