package kr.or.kosa.snippets.like.service;

import kr.or.kosa.snippets.like.mapper.LikeSnippetMapper;
import kr.or.kosa.snippets.like.mapper.TagMapper;
import kr.or.kosa.snippets.like.model.LikeTag;
import kr.or.kosa.snippets.like.model.Snippet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LikeSnippetService {

    @Autowired
    private LikeSnippetMapper likeSnippetMapper;

    @Autowired
    private TagMapper tagMapper;

    // ===== 기존 메서드들 =====

    // 인기 스니펫 조회 (기본 테이블 방식)
    public List<Snippet> getPopularSnippets(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 100; // 기본값 설정
        }
        return likeSnippetMapper.getPopularSnippets(limit);
    }

    // 검색 기능 - 전체 DB에서 직접 검색
    public List<Snippet> searchSnippets(String type, String keyword, Integer minLikes, String tagName) {
        // 검색 조건이 모두 비어있으면 기본적으로 인기순 100개 반환
        if ((type == null || type.isEmpty()) &&
                (keyword == null || keyword.isEmpty()) &&
                (minLikes == null || minLikes <= 0) &&
                (tagName == null || tagName.isEmpty())) {
            return getPopularSnippets(100);
        }

        // 최소 좋아요 수가 null이면 0으로 설정
        if (minLikes == null) {
            minLikes = 0;
        }

        // 검색을 수행하고 결과 반환
        return likeSnippetMapper.searchSnippets(type, keyword, minLikes, tagName);
    }

    // 모든 태그 조회
    public List<LikeTag> getAllTags() {
        return tagMapper.getAllTags();
    }

    // 특정 스니펫의 태그 조회
    public List<LikeTag> getTagsBySnippetId(Integer snippetId) {
        return tagMapper.getTagsBySnippetId(snippetId);
    }

    // 스니펫 상세 조회
    public Snippet getSnippetDetailById(Integer snippetId) {
        return likeSnippetMapper.getSnippetDetailById(snippetId);
    }

    // ===== 기존 테이블 기반 메서드들로 수정 =====

    /**
     * 인기 스니펫 조회 (기존 테이블 방식, 상위 100개로 제한)
     */
    public List<Snippet> getPopularSnippetsFromView(Integer limit) {
        // 뷰 대신 기존 테이블 방식 사용
        if (limit == null || limit > 100) {
            limit = 100;
        }
        return likeSnippetMapper.getPopularSnippets(limit);
    }

    /**
     * 상위 100개 인기 스니펫 조회 (popular_snippets_view 사용)
     */
    public List<Snippet> getTop100PopularSnippets(Integer limit) {
        if (limit == null || limit > 100) {
            limit = 100;
        }
        return likeSnippetMapper.getPopularSnippetsFromView(limit);
    }

    /**
     * 페이징 처리된 인기 스니펫 조회 (기존 테이블 방식)
     */
    public List<Snippet> getPopularSnippetsPagedFromView(Integer offset, Integer limit) {
        // 100개 범위 체크
        if (offset >= 100) {
            return new ArrayList<>();  // 100개 초과 시 빈 리스트 반환
        }

        // 100개 범위 내에서만 페이징 처리
        int maxLimit = Math.min(limit, 100 - offset);
        if (maxLimit <= 0) {
            return new ArrayList<>();
        }

        // 기존 테이블 방식의 페이징 사용
        return likeSnippetMapper.getPopularSnippetsPaged(offset, maxLimit);
    }

    /**
     * 전체 인기 스니펫 수 조회 (최대 100개로 제한)
     */
    public int countPopularSnippetsFromView() {
        // 기존 테이블에서 인기 스니펫 100개의 개수 반환
        List<Snippet> popularSnippets = likeSnippetMapper.getPopularSnippets(100);
        return Math.min(popularSnippets.size(), 100);
    }

    /**
     * TOP 100 스니펫 내에서 필터링 (인메모리 방식)
     */
    public List<Snippet> filterSnippetsFromTop100(String type, String keyword, Integer minLikes, String tagName) {
        // 먼저 TOP 100 스니펫을 가져옴 (popular_snippets_view 사용)
        List<Snippet> allPopularSnippets = getPopularSnippetsFromView(100);

        // 필터링 조건이 모두 없으면 그대로 반환
        if ((type == null || type.isEmpty()) &&
                (keyword == null || keyword.isEmpty()) &&
                (minLikes == null || minLikes <= 0) &&
                (tagName == null || tagName.isEmpty())) {
            return allPopularSnippets;
        }

        // 유형, 키워드, 좋아요 수로 필터링
        List<Snippet> filteredSnippets = allPopularSnippets.stream()
                .filter(s -> type == null || type.isEmpty() || s.getType().equals(type))
                .filter(s -> keyword == null || keyword.isEmpty() ||
                        (s.getMemo() != null && s.getMemo().toLowerCase().contains(keyword.toLowerCase())))
                .filter(s -> minLikes == null || s.getLikeCount() >= minLikes)
                .collect(Collectors.toList());

        // 태그 필터링 (필요한 경우)
        if (tagName != null && !tagName.isEmpty()) {
            filteredSnippets = filteredSnippets.stream()
                    .filter(s -> {
                        List<LikeTag> snippetTags = getTagsBySnippetId(s.getSnippetId());
                        return snippetTags.stream()
                                .anyMatch(tag -> tag.getName().equalsIgnoreCase(tagName));
                    })
                    .collect(Collectors.toList());
        }

        return filteredSnippets;
    }
}