package kr.or.kosa.snippets.like.controller;

import kr.or.kosa.snippets.like.model.Tag;
import kr.or.kosa.snippets.like.service.SnippetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tags")
public class TagRestController {

    @Autowired
    private SnippetService snippetService;

    /**
     * 태그 검색 API - 자동완성용
     * @param query 검색어
     * @return 매칭되는 태그 목록 (중복 제거)
     */
    @GetMapping("/search")
    public List<String> searchTags(@RequestParam(value = "query", defaultValue = "") String query) {
        List<Tag> allTags = snippetService.getAllTags();

        // 검색어가 비어있으면 모든 태그 반환 (중복 제거)
        if (query.trim().isEmpty()) {
            return allTags.stream()
                    .map(Tag::getName)
                    .distinct()  // 중복 제거
                    .collect(Collectors.toList());
        }

        // 검색어로 필터링 (대소문자 구분 없이) + 중복 제거
        return allTags.stream()
                .filter(tag -> tag.getName().toLowerCase().contains(query.toLowerCase()))
                .map(Tag::getName)
                .distinct()  // 중복 제거
                .collect(Collectors.toList());
    }

    /**
     * 모든 태그 목록 조회 (중복 제거)
     */
    @GetMapping("/all")
    public List<String> getAllTags() {
        return snippetService.getAllTags().stream()
                .map(Tag::getName)
                .distinct()  // 중복 제거
                .collect(Collectors.toList());
    }
}