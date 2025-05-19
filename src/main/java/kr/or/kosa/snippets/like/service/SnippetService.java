package kr.or.kosa.snippets.like.service;

import kr.or.kosa.snippets.like.mapper.SnippetMapper;
import kr.or.kosa.snippets.like.mapper.TagMapper;
import kr.or.kosa.snippets.like.model.Snippet;
import kr.or.kosa.snippets.like.model.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

// ===== 뷰 방식 메서드들 (상위 100개로 제한) =====

    /**
     * 뷰를 이용한 인기 스니펫 조회 (상위 100개 중에서 limit만큼)
     */
    public List<Snippet> getPopularSnippetsFromView(Integer limit) {
        // 뷰 자체가 상위 100개로 제한되어 있으므로 추가 제한만 적용
        if (limit == null || limit > 100) {
            limit = 100;
        }
        return snippetMapper.getPopularSnippetsFromView(limit);
    }

    /**
     * 상위 100개 뷰에서 제한된 인기 스니펫 조회
     */
    public List<Snippet> getTop100PopularSnippets(Integer limit) {
        // 이미 100개로 제한된 뷰에서 추가로 제한
        if (limit == null || limit > 100) {
            limit = 100;
        }
        return snippetMapper.getTop100PopularSnippets().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 뷰를 이용한 페이징 처리된 인기 스니펫 조회 (상위 100개 내에서만)
     */
    public List<Snippet> getPopularSnippetsPagedFromView(Integer offset, Integer limit) {
        // 뷰가 상위 100개로 제한되어 있으므로 100개 내에서만 페이징
        if (offset >= 100) {
            return new ArrayList<>();  // 100개 초과 시 빈 리스트 반환
        }

        // 100개 범위 내에서만 페이징 처리
        int maxLimit = Math.min(limit, 100 - offset);
        if (maxLimit <= 0) {
            return new ArrayList<>();
        }

        return snippetMapper.getPopularSnippetsPagedFromView(offset, maxLimit);
    }

    /**
     * 뷰에서 전체 인기 스니펫 수 조회 (최대 100개)
     */
    public int countPopularSnippetsFromView() {
        // 뷰가 최대 100개로 제한되어 있음
        return Math.min(snippetMapper.countPopularSnippetsFromView(), 100);
    }

    /**
     * 뷰를 이용한 최신순 조회 (비교용)
     */
    public List<Snippet> getRecentSnippetsFromView(Integer limit) {
        return snippetMapper.getRecentSnippetsFromView(limit);
    }
}