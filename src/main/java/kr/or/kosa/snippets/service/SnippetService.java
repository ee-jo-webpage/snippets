package kr.or.kosa.snippets.service;

import kr.or.kosa.snippets.mapper.SnippetMapper;
import kr.or.kosa.snippets.mapper.TagMapper;
import kr.or.kosa.snippets.model.Snippet;
import kr.or.kosa.snippets.model.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SnippetService {

    @Autowired
    private SnippetMapper snippetMapper;

    @Autowired
    private TagMapper tagMapper;

    // 인기 스니펫 조회
    public List<Snippet> getPopularSnippets(Integer limit) {
        return snippetMapper.getPopularSnippets(limit);
    }

    // 검색 기능
    public List<Snippet> searchSnippets(String type, String keyword, Integer minLikes, String tagName) {
        return snippetMapper.searchSnippets(type, keyword, minLikes, tagName);
    }

    // 모든 태그 조회
    public List<Tag> getAllTags() {
        return tagMapper.getAllTags();
    }

    // 특정 스니펫의 태그 조회
    public List<Tag> getTagsBySnippetId(Integer snippetId) {
        return tagMapper.getTagsBySnippetId(snippetId);
    }

    // 스니펫 상세 조회
    public Snippet getSnippetDetailById(Integer snippetId) {
        return snippetMapper.getSnippetDetailById(snippetId);
    }

    // SnippetService.java에 추가
    public List<Snippet> getPopularSnippetsJavaSorted(Integer limit) {
        // 일단 모든 스니펫을 가져온 후
        List<Snippet> snippets = snippetMapper.getAllPublicSnippets();

        // 좋아요 수를 기준으로 내림차순 정렬 (Java 정렬 사용)
        snippets.sort((s1, s2) -> Integer.compare(s2.getLikeCount(), s1.getLikeCount()));

        // limit 개수만큼만 반환
        return snippets.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

}