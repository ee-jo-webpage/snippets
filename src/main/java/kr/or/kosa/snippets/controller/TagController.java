package kr.or.kosa.snippets.controller;

import kr.or.kosa.snippets.model.Snippet;
import kr.or.kosa.snippets.model.Tag;
import kr.or.kosa.snippets.service.SnippetService;
import kr.or.kosa.snippets.service.TagServiceImpl;
import org.apache.coyote.Response;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tag")
public class TagController {

    @Autowired
    private TagServiceImpl tagService;

    @Autowired
    private SnippetService snippetService;  // 스니펫 서비스 추가

    //모든 태그 조회
    @GetMapping
    public ResponseEntity<List<Tag>> getAllTags() {
        List<Tag> tags = tagService.selectAllTags();

        return ResponseEntity.ok(tags);
    }

    //태그 ID로 태그 조회
    @GetMapping("/{tagId}")
    public ResponseEntity<Tag> getTagById(@PathVariable("tagId") Long tagId) {
        Tag tag = tagService.selectTagByTagId(tagId);

        if (tag != null) {
            return ResponseEntity.ok(tag);
        }

        return ResponseEntity.notFound().build();
    }

    //태그 이름으로 태그 조회
    @GetMapping("/name/{name}")
    public ResponseEntity<Tag> getTagByName(@PathVariable("name") String name) {

        String findingName = name.toLowerCase();
        Tag tag = tagService.selectTagByName(name);
        String comparedName = tag.getName().toLowerCase();

        if (findingName == comparedName) {
            return ResponseEntity.ok(tag);
        }

        return ResponseEntity.notFound().build();
    }

    //새 태그 생성
    @PostMapping
    public ResponseEntity<Tag> createTag(@RequestBody Tag tag) {

        //태그명 중복확인
        Tag existingTag = tagService.selectTagByName(tag.getName());
        if (existingTag != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(existingTag);
        }

        Tag createdTag = tagService.createTag(tag.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdTag);
    }

    //태그 수정
    @PutMapping("/{tagId}")
    public ResponseEntity<Tag> updateTag(@PathVariable Long tagId,
                                         @RequestBody Tag tag) {
        Tag existingTag = tagService.selectTagByTagId(tagId);

        if (existingTag == null) {
            return ResponseEntity.notFound().build();
        }

        //이름 중복 체크(본인 제외)
        Tag duplicateTag = tagService.selectTagByName(tag.getName());
        if (duplicateTag != null && !duplicateTag.getTagId().equals(tagId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Tag updatedTag = tagService.updateTag(tagId, tag.getName());

        return ResponseEntity.ok(updatedTag);
    }

    //태그 삭제
    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long tagId) {
        boolean deleted = tagService.deleteTag(tagId);

        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
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

    //특정 스니펫의 모든 태그 조회
    @GetMapping("/snippets/{snippetId}")
    public ResponseEntity<List<Tag>> getTagBySnippetId(@PathVariable Long snippetId) {
        List<Tag> tags = tagService.selectTagsBySnippetId(snippetId);

        return ResponseEntity.ok(tags);
    }

    //특정 태그를 가진 스니펫 목록 (수정됨)
    @GetMapping("/{tagId}/snippets")
    public ResponseEntity<List<Snippet>> getSnippetsByTagId(@PathVariable Long tagId) {
        // 스니펫 ID 목록이 아닌 실제 스니펫 정보를 반환
        List<Snippet> snippets = tagService.selectSnippetsByTagId(tagId);
        return ResponseEntity.ok(snippets);
    }


    /**
     * 태그별 스니펫 ID 조회 페이지 (간단한 목록만)
     */
    @GetMapping("/tags/snippets")
    public String showTagSnippets(@RequestParam(required = false) Long tagId, Model model) {
        try {
            // 모든 태그 조회 (필터용)
            List<Tag> allTags = tagService.selectAllTags();
            model.addAttribute("allTags", allTags);

            if (tagId != null) {
                // 특정 태그 선택된 경우
                Tag selectedTag = tagService.selectTagByTagId(tagId);
                if (selectedTag != null) {
                    model.addAttribute("selectedTag", selectedTag);

                    // 해당 태그의 스니펫 ID 목록만 조회
                    List<Snippet> snippetIds = tagService.selectSnippetIdsByTagId(tagId);
                    model.addAttribute("snippetIds", snippetIds);
                    model.addAttribute("totalSnippetsForTag", snippetIds.size());
                }
            } else {
                // 전체 태그와 연결된 스니펫 ID들 조회
                model.addAttribute("snippetIds", new ArrayList<>());
            }

        } catch (Exception e) {
            model.addAttribute("error", "태그 조회 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("snippetIds", new ArrayList<>());
        }

        return "tag-snippets";
    }

    // 태그 검색 API 수정 (URL 중복 제거)
    @GetMapping("/search")
    @ResponseBody
    public List<Tag> searchTags(@RequestParam String query) {
        List<Tag> allTags = tagService.selectAllTags();
        return allTags.stream()
                .filter(tag -> tag.getName().toLowerCase().contains(query.toLowerCase()))
                .limit(10)
                .collect(Collectors.toList());
    }
}
