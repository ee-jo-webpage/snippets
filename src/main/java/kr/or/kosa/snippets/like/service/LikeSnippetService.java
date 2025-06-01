package kr.or.kosa.snippets.like.service;

import kr.or.kosa.snippets.like.mapper.LikeSnippetMapper;
import kr.or.kosa.snippets.like.mapper.SnippetContentMapper;
import kr.or.kosa.snippets.like.mapper.TagMapper;
import kr.or.kosa.snippets.like.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LikeSnippetService {

    @Autowired
    private LikeSnippetMapper likeSnippetMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private SnippetContentMapper snippetContentMapper;

    // ===== 기존 메서드들 =====

    // 인기 스니펫 조회 (기본 테이블 방식)
    public List<Snippet> getPopularSnippets(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 100; // 기본값 설정
        }
        return likeSnippetMapper.getPopularSnippets(limit);
    }

    // 검색 기능 - 전체 DB에서 직접 검색 (상위 100개로 제한)
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

        // 검색을 수행하고 결과 반환 (이미 쿼리에서 100개로 제한됨)
        List<Snippet> results = likeSnippetMapper.searchSnippets(type, keyword, minLikes, tagName);

        // 추가 안전장치: 만약 100개를 넘는다면 상위 100개만 반환
        if (results.size() > 100) {
            return results.subList(0, 100);
        }

        return results;
    }

    // 모든 태그 조회
    public List<LikeTag> getAllTags() {
        return tagMapper.getAllTags();
    }

    // 특정 스니펫의 태그 조회
    public List<LikeTag> getTagsBySnippetId(Long snippetId) {
        return tagMapper.getTagsBySnippetId(snippetId);
    }

    // 스니펫 상세 조회
    public Snippet getSnippetDetailById(Long snippetId) {
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
     * TOP 100 스니펫 내에서 필터링 (인메모리 방식) - 내용 검색으로 수정
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

        // 유형, 좋아요 수로 먼저 필터링
        List<Snippet> filteredSnippets = allPopularSnippets.stream()
                .filter(s -> type == null || type.isEmpty() || s.getType().equals(type))
                .filter(s -> minLikes == null || s.getLikeCount() >= minLikes)
                .collect(Collectors.toList());

        // 키워드 필터링 (내용 검색)
        if (keyword != null && !keyword.isEmpty()) {
            filteredSnippets = filteredSnippets.stream()
                    .filter(s -> matchesContentKeyword(s, keyword))
                    .collect(Collectors.toList());
        }

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

    /**
     * 스니펫 내용에서 키워드가 포함되어 있는지 검사하는 메서드
     */
    private boolean matchesContentKeyword(Snippet snippet, String keyword) {
        try {
            String lowerKeyword = keyword.toLowerCase();

            switch (snippet.getType().toUpperCase()) {
                case "CODE":
                    SnippetCode snippetCode = snippetContentMapper.getSnippetCodeById(snippet.getSnippetId());
                    if (snippetCode != null && snippetCode.getContent() != null) {
                        return snippetCode.getContent().toLowerCase().contains(lowerKeyword);
                    }
                    break;

                case "TEXT":
                    SnippetText snippetText = snippetContentMapper.getSnippetTextById(snippet.getSnippetId());
                    if (snippetText != null && snippetText.getContent() != null) {
                        return snippetText.getContent().toLowerCase().contains(lowerKeyword);
                    }
                    break;

                case "IMG":
                    SnippetImage snippetImage = snippetContentMapper.getSnippetImageById(snippet.getSnippetId());
                    if (snippetImage != null && snippetImage.getAltText() != null) {
                        return snippetImage.getAltText().toLowerCase().contains(lowerKeyword);
                    }
                    break;
            }
        } catch (Exception e) {
            System.err.println("키워드 매칭 실패 - snippetId: " + snippet.getSnippetId() + ", 오류: " + e.getMessage());
        }

        return false;
    }

    /**
     * 스니펫의 content 미리보기 조회 (타입별로 다른 테이블에서)
     */
    public String getSnippetContentPreview(Snippet snippet) {
        try {
            switch (snippet.getType().toUpperCase()) {
                case "CODE":
                    SnippetCode snippetCode = snippetContentMapper.getSnippetCodeById(snippet.getSnippetId());
                    if (snippetCode != null && snippetCode.getContent() != null) {
                        return truncateContent(snippetCode.getContent(), 100); // 100자로 제한
                    }
                    break;

                case "TEXT":
                    SnippetText snippetText = snippetContentMapper.getSnippetTextById(snippet.getSnippetId());
                    if (snippetText != null && snippetText.getContent() != null) {
                        return truncateContent(snippetText.getContent(), 100); // 100자로 제한
                    }
                    break;

                case "IMG":
                    SnippetImage snippetImage = snippetContentMapper.getSnippetImageById(snippet.getSnippetId());
                    if (snippetImage != null) {
                        return "[이미지] " + (snippetImage.getAltText() != null ? snippetImage.getAltText() : "이미지 설명 없음");
                    }
                    break;

                default:
                    return "내용을 불러올 수 없습니다.";
            }
        } catch (Exception e) {
            System.err.println("스니펫 content 조회 실패 - snippetId: " + snippet.getSnippetId() + ", 오류: " + e.getMessage());
        }

        return "내용을 불러올 수 없습니다.";
    }

    /**
     * 내용을 지정된 길이로 자르고 "..." 추가
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null) {
            return "";
        }

        // 줄바꿈 문자를 공백으로 변경
        content = content.replaceAll("\\r?\\n", " ");

        if (content.length() <= maxLength) {
            return content;
        }

        return content.substring(0, maxLength) + "...";
    }

    /**
     * 여러 스니펫의 content를 한 번에 조회 (성능 최적화)
     */
    public Map<Long, String> getSnippetContentPreviews(List<Snippet> snippets) {
        Map<Long, String> contentMap = new HashMap<>();

        for (Snippet snippet : snippets) {
            String content = getSnippetContentPreview(snippet);
            contentMap.put(snippet.getSnippetId(), content);
        }

        return contentMap;
    }
}