package kr.or.kosa.snippets.tag.controller;

import kr.or.kosa.snippets.basic.service.SnippetService;
import kr.or.kosa.snippets.tag.model.TagItem;
import kr.or.kosa.snippets.tag.service.TagService;
import kr.or.kosa.snippets.user.service.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    private Long requireLogin(CustomUserDetails userDetails) {
        if (userDetails == null) {

            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다");
        }
        return userDetails.getUserId();
    }

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

    //스니펫에 태그 추가
    @PostMapping("/snippet/{snippetId}/tags/{tagId}")
    public ResponseEntity<Void> addTagToSnippet(@PathVariable Long snippetId,
                                                @PathVariable Long tagId) {
        boolean added = tagService.addTagToSnippet(snippetId, tagId);

        if (added) {
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    //특정 스니펫의 모든 태그 조회
    // TagController.java 파일 수정
    @GetMapping("/snippets/{snippetId}")
    public ResponseEntity<List<TagItem>> getTagBySnippetId(@PathVariable Long snippetId) {
        if (snippetId == null) {
            return ResponseEntity.badRequest().build();
        }

        System.out.println("스니펫 ID: " + snippetId + " 태그 조회 요청");
        List<TagItem> tags = tagService.getTagsBySnippetId(snippetId);
        System.out.println("스니펫 ID: " + snippetId + ", 태그 수: " + (tags != null ? tags.size() : "null"));

        return ResponseEntity.ok(tags != null ? tags : new ArrayList<>());
    }

    //스니펫에서 태그 제거
    @DeleteMapping("/snippets/{snippetId}/tags/{tagId}")
    public ResponseEntity<Void> removeTagFromSnippet(@PathVariable Long snippetId,
                                                     @PathVariable Long tagId) {
        boolean removed = tagService.removeTagFromSnippet(snippetId, tagId);

        if (removed) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();

    }




}
