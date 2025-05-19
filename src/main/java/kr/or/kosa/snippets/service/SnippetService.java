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

    // ===== 기존 메서드들 =====

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

    // 기존 Java 정렬 방식
    public List<Snippet> getPopularSnippetsJavaSorted(Integer limit) {
        List<Snippet> snippets = snippetMapper.getAllPublicSnippets();
        snippets.sort((s1, s2) -> Integer.compare(s2.getLikeCount(), s1.getLikeCount()));
        return snippets.stream().limit(limit).collect(Collectors.toList());
    }

    // ===== 성능 테스트용 추가 메서드들 =====

    /**
     * 모든 공개 스니펫 조회 (성능 테스트용)
     */
    public List<Snippet> getAllPublicSnippets() {
        return snippetMapper.getAllPublicSnippets();
    }

    /**
     * 제한된 개수의 공개 스니펫 조회 (성능 테스트용)
     */
    public List<Snippet> getAllPublicSnippets(Integer limit) {
        return snippetMapper.getAllPublicSnippets().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 특정 사용자의 스니펫 조회 (성능 테스트용)
     */
    public List<Snippet> getSnippetsByUserId(Integer userId) {
        return snippetMapper.getSnippetsByUserId(userId);
    }

    /**
     * 페이징 처리된 공개 스니펫 조회 (성능 테스트용)
     */
    public List<Snippet> getAllPublicSnippetsPaged(Integer offset, Integer limit) {
        return snippetMapper.getAllPublicSnippetsPaged(offset, limit);
    }

    // ===== 뷰 방식 메서드들 (비교용) =====

    /**
     * 뷰를 이용한 인기 스니펫 조회
     */
    public List<Snippet> getPopularSnippetsFromView(Integer limit) {
        return snippetMapper.getPopularSnippetsFromView(limit);
    }

    /**
     * 상위 100개 뷰에서 제한된 인기 스니펫 조회
     */
    public List<Snippet> getTop100PopularSnippets(Integer limit) {
        return snippetMapper.getTop100PopularSnippets().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 뷰를 이용한 페이징 처리된 인기 스니펫 조회
     */
    public List<Snippet> getPopularSnippetsPagedFromView(Integer offset, Integer limit) {
        return snippetMapper.getPopularSnippetsPagedFromView(offset, limit);
    }

    /**
     * 뷰에서 전체 인기 스니펫 수 조회
     */
    public int countPopularSnippetsFromView() {
        return snippetMapper.countPopularSnippetsFromView();
    }

    /**
     * 뷰를 이용한 최신순 조회 (비교용)
     */
    public List<Snippet> getRecentSnippetsFromView(Integer limit) {
        return snippetMapper.getRecentSnippetsFromView(limit);
    }
}