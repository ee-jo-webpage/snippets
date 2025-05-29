package kr.or.kosa.snippets.tag.service;

import kr.or.kosa.snippets.basic.model.Snippets;
import kr.or.kosa.snippets.basic.model.SnippetTypeBasic;
import kr.or.kosa.snippets.basic.service.SnippetService;
import kr.or.kosa.snippets.tag.mapper.TagMapper;
import kr.or.kosa.snippets.tag.model.SnippetTag;
import kr.or.kosa.snippets.tag.model.TagItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class TagService {

    @Autowired
    TagMapper tagMapper;

    @Autowired
    SnippetService snippetService;  // SnippetService 추가

    // 사용자Id로 모든 태그 조회
    @Transactional(readOnly = true)
    public List<TagItem> getTagsByUserId(Long userId) {
        return tagMapper.selectTagsByUserId(userId);
    }

    // 사용자 Id와 태그 이름으로 태그 조회
    @Transactional(readOnly = true)
    public TagItem getTagByUserIdAndName(Long userId, String name) {
        return tagMapper.selectTagByUserIdAndName(userId, name);
    }

    // 태그Id로 태그 조회
    @Transactional(readOnly = true)
    public TagItem selectTagByTagId(Long tagId) {
        return tagMapper.selectTagById(tagId);
    }

    // 사용자별 태그 생성
    @Transactional
    public TagItem createTag(Long userId, String name) {
        // 중복 태그 확인
        TagItem existingTag = tagMapper.selectTagByUserIdAndName(userId, name);
        if (existingTag != null) {
            throw new IllegalArgumentException("이미 존재하는 태그입니다: " + name);
        }

        TagItem newTag = new TagItem(userId, name);
        int result = tagMapper.insertTag(newTag);
        if (result > 0) {
            // 생성된 태그 정보 반환 (auto-generated key 포함)
            return tagMapper.selectTagByUserIdAndName(userId, name);
        }

        throw new RuntimeException("태그 생성에 실패했습니다");
    }

    // 스니펫에 태그 추가
    @Transactional
    public boolean addTagToSnippet(Long snippetId, Long tagId) {
        // 이미 연결되어 있는지 확인
        if (tagMapper.countSnippetTag(snippetId, tagId) == 0) {
            SnippetTag snippetTag = new SnippetTag(snippetId, tagId);
            int result = tagMapper.insertSnippetTag(snippetTag);
            return result > 0;
        }
        // 이미 연결되어 있으면 false 반환
        return false;
    }

    // 특정 스니펫의 모든 태그 조회
    @Transactional(readOnly = true)
    public List<TagItem> getTagsBySnippetId(Long snippetId) {
        return tagMapper.selectTagBySnippetId(snippetId);
    }

    // 사용자의 태그 삭제 (권한 확인 포함)
    @Transactional
    public boolean deleteTag(Long tagId, Long userId) {
        TagItem tag = tagMapper.selectTagById(tagId);

        if (tag != null && tag.getUserId().equals(userId)) {
            // 먼저 snippet_tags에서 관련 레코드 삭제
            tagMapper.deleteSnippetTagsByTagId(tagId);
            // 그 다음 태그 삭제
            int result = tagMapper.deleteTag(tagId, userId);
            return result > 0;
        }

        return false;
    }

    // 스니펫에서 태그 제거
    @Transactional
    public boolean removeTagFromSnippet(Long snippetId, Long tagId) {
        return tagMapper.deleteSnippetTag(snippetId, tagId) > 0;
    }

    // 사용자가 특정 태그를 소유하고 있는지 확인
    @Transactional(readOnly = true)
    public boolean isTagOwnedByUser(Long tagId, Long userId) {
        TagItem tag = tagMapper.selectTagById(tagId);
        return tag != null && tag.getUserId().equals(userId);
    }

    // 특정 스니펫 조회 - SnippetService 사용하도록 개선
    @Transactional(readOnly = true)
    public Snippets getSnippetById(Long snippetId) {
        try {
            // 타입을 먼저 조회
            SnippetTypeBasic type = snippetService.getSnippetTypeById(snippetId);
            if (type != null) {
                // 타입별 완전한 스니펫 정보 조회
                return snippetService.getSnippetsById(snippetId, type);
            }
        } catch (Exception e) {
            log.error("스니펫 조회 중 오류 발생 - snippetId: {}", snippetId, e);
        }
        return null;
    }

    // 모든 스니펫 조회 - SnippetService로 위임
    @Transactional(readOnly = true)
    public List<Snippets> getAllSnippets() {
        return snippetService.getAllSnippets();
    }

    // 특정 사용자가 작성한 모든 스니펫 조회 - SnippetService로 위임하되 완전한 정보 포함
    @Transactional(readOnly = true)
    public List<Snippets> getSnippetsByUserId(Long userId) {
        return snippetService.getUserSnippets(userId);
    }

    // 특정 태그가 연결된 스니펫 수 조회
    @Transactional(readOnly = true)
    public int getSnippetCountByTagId(Long tagId) {
        return tagMapper.countSnippetsByTagId(tagId);
    }

    // 사용자가 가장 많이 사용한 태그 조회 (상위 N개) - 추가
    @Transactional(readOnly = true)
    public List<TagItem> getMostUsedTagsByUserId(Long userId, int limit) {
        return tagMapper.selectMostUsedTagsByUserId(userId, limit);
    }

    // 특정 태그로 스니펫 검색 - SnippetService 사용하도록 개선 (색상 정보 포함)
    @Transactional(readOnly = true)
    public List<Snippets> getSnippetsByTagId(Long tagId) {
        // 1. 태그와 연결된 스니펫 기본 정보 조회 (색상 정보 포함)
        List<Snippets> basicSnippets = tagMapper.selectSnippetsByTagIdWithColor(tagId);
        List<Snippets> completeSnippets = new ArrayList<>();

        // 2. 각 스니펫에 대해 완전한 정보를 SnippetService를 통해 조회
        for (Snippets snippet : basicSnippets) {
            try {
                // 색상 정보 임시 저장
                String colorName = snippet.getName();
                String hexCode = snippet.getHexCode();

                // 타입을 먼저 조회
                SnippetTypeBasic type = snippetService.getSnippetTypeById(snippet.getSnippetId());
                if (type != null) {
                    // 타입별 완전한 스니펫 정보 조회
                    Snippets completeSnippet = snippetService.getSnippetsById(snippet.getSnippetId(), type);
                    if (completeSnippet != null) {
                        // 색상 정보 복원 (조인으로 가져온 정보)
                        completeSnippet.setName(colorName);
                        completeSnippet.setHexCode(hexCode);
                        completeSnippets.add(completeSnippet);
                    }
                }
            } catch (Exception e) {
                log.error("태그별 스니펫 조회 중 오류 발생 - snippetId: {}", snippet.getSnippetId(), e);
                // 오류가 발생한 스니펫은 건너뛰지만 나머지는 계속 처리
            }
        }

        return completeSnippets;
    }

    // 여러 태그로 스니펫 검색 (AND 조건) - SnippetService 사용하도록 개선 (색상 정보 포함)
    @Transactional(readOnly = true)
    public List<Snippets> getSnippetsByMultipleTags(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return List.of();
        }

        // 1. 여러 태그와 모두 연결된 스니펫 기본 정보 조회 (색상 정보 포함)
        List<Snippets> basicSnippets = tagMapper.selectSnippetsByMultipleTagsWithColor(tagIds);
        List<Snippets> completeSnippets = new ArrayList<>();

        // 2. 각 스니펫에 대해 완전한 정보를 SnippetService를 통해 조회
        for (Snippets snippet : basicSnippets) {
            try {
                // 색상 정보 임시 저장
                String colorName = snippet.getName();
                String hexCode = snippet.getHexCode();

                // 타입을 먼저 조회
                SnippetTypeBasic type = snippetService.getSnippetTypeById(snippet.getSnippetId());
                if (type != null) {
                    // 타입별 완전한 스니펫 정보 조회
                    Snippets completeSnippet = snippetService.getSnippetsById(snippet.getSnippetId(), type);
                    if (completeSnippet != null) {
                        // 색상 정보 복원 (조인으로 가져온 정보)
                        completeSnippet.setName(colorName);
                        completeSnippet.setHexCode(hexCode);
                        completeSnippets.add(completeSnippet);
                    }
                }
            } catch (Exception e) {
                log.error("다중 태그 스니펫 조회 중 오류 발생 - snippetId: {}", snippet.getSnippetId(), e);
                // 오류가 발생한 스니펫은 건너뛰지만 나머지는 계속 처리
            }
        }

        return completeSnippets;
    }

    // 태그명으로 검색 (사용자별) - 추가
    @Transactional(readOnly = true)
    public List<TagItem> searchTagsByKeyword(Long userId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        return tagMapper.searchTagsByKeyword(userId, keyword.trim());
    }

    // 태그 사용 통계 조회 - 추가
    @Transactional(readOnly = true)
    public Map<String, Object> getTagStatistics(Long userId) {
        List<TagItem> allTags = getTagsByUserId(userId);
        List<TagItem> mostUsedTags = getMostUsedTagsByUserId(userId, 5);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTags", allTags.size());
        stats.put("mostUsedTags", mostUsedTags);
        stats.put("averageTagsPerSnippet", calculateAverageTagsPerSnippet(userId));

        return stats;
    }

    // 사용자별 평균 태그 수 계산 - 추가 (private 메소드)
    private double calculateAverageTagsPerSnippet(Long userId) {
        List<Snippets> snippets = getSnippetsByUserId(userId);
        if (snippets.isEmpty()) {
            return 0.0;
        }

        int totalTags = 0;
        for (Snippets snippet : snippets) {
            totalTags += getTagsBySnippetId(snippet.getSnippetId()).size();
        }

        return (double) totalTags / snippets.size();
    }
}