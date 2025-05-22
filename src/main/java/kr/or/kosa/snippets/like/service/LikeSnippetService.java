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

    // 인기 스니펫 조회
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


// ===== 뷰 방식 메서드들 (상위 100개로 제한) =====

    /**
     * 뷰를 이용한 인기 스니펫 조회 (상위 100개 중에서 limit만큼)
     */
    public List<Snippet> getPopularSnippetsFromView(Integer limit) {
        // 뷰 자체가 상위 100개로 제한되어 있으므로 추가 제한만 적용
        if (limit == null || limit > 100) {
            limit = 100;
        }
        return likeSnippetMapper.getPopularSnippetsFromView(limit);
    }

    /**
     * 상위 100개 뷰에서 제한된 인기 스니펫 조회
     */
    public List<Snippet> getTop100PopularSnippets(Integer limit) {
        // 이미 100개로 제한된 뷰에서 추가로 제한
        if (limit == null || limit > 100) {
            limit = 100;
        }
        return likeSnippetMapper.getTop100PopularSnippets().stream()
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

        return likeSnippetMapper.getPopularSnippetsPagedFromView(offset, maxLimit);
    }

    /**
     * 뷰에서 전체 인기 스니펫 수 조회 (최대 100개)
     */
    public int countPopularSnippetsFromView() {
        // 뷰가 최대 100개로 제한되어 있음
        return Math.min(likeSnippetMapper.countPopularSnippetsFromView(), 100);
    }

    /**
     * 뷰에서 가져온 TOP 100 스니펫 내에서 필터링 (클라이언트 사이드)
     * 이 메서드는 필요한 경우 서비스 측에서 인메모리 필터링을 수행하기 위한 것입니다.
     */
    public List<Snippet> filterSnippetsFromTop100(String type, String keyword, Integer minLikes, String tagName) {
        // 먼저 TOP 100 스니펫을 가져옴
        List<Snippet> allPopularSnippets = getTop100PopularSnippets(100);

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
