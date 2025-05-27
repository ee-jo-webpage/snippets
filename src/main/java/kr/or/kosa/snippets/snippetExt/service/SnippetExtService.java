package kr.or.kosa.snippets.snippetExt.service;

import jakarta.persistence.EntityNotFoundException;
import kr.or.kosa.snippets.snippetExt.exception.DuplicateSnippetException;
import kr.or.kosa.snippets.snippetExt.mapper.SnippetExtMapper;
import kr.or.kosa.snippets.snippetExt.model.ColorExt;
import kr.or.kosa.snippets.snippetExt.model.PopSnippets;
import kr.or.kosa.snippets.snippetExt.model.SnippetExtCreate;
import kr.or.kosa.snippets.snippetExt.model.SnippetExtUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SnippetExtService {

    private final SnippetExtMapper snippetExtMapper;

    // ================================================
    // public
    // ================================================

    @Transactional
    public Long save(SnippetExtCreate snippet) {
        if (isDuplicate(snippet)) {
            throw new DuplicateSnippetException("이미 동일한 스니펫이 존재합니다.");
        }

        validateSnippet(snippet);           // 유효성 검사
        snippetExtMapper.insertSnippet(snippet); // 공통 삽입
        insertDetailByType(snippet);        // 타입별 분기 삽입

        return snippet.getSnippetId();
    }

    public void updateSnippet(SnippetExtUpdate snippetUpdate, Long requesterId) {
        Long ownerId = getOwnerIdOrThrow(snippetUpdate.getSnippetId());
        validateOwnership(ownerId, requesterId, "수정 권한이 없습니다.");

        snippetUpdate.setUserId(requesterId);
        snippetExtMapper.updateSnippet(snippetUpdate);
    }

    public void deleteSnippet(Long snippetId, Long requesterId) {
        Long ownerId = getOwnerIdOrThrow(snippetId);
        validateOwnership(ownerId, requesterId, "삭제 권한이 없습니다.");

        snippetExtMapper.deleteSnippet(snippetId);
    }

    // ================================================
    // private 내부 로직
    // ================================================

    private boolean isDuplicate(SnippetExtCreate snippet) {
        return snippetExtMapper.countDuplicate(snippet) > 0;
    }

    private void validateSnippet(SnippetExtCreate snippet) {
        switch (snippet.getType()) {
            case TEXT -> {
                if (isBlank(snippet.getContent())) {
                    throw new IllegalArgumentException("TEXT 스니펫은 content가 필수입니다.");
                }
            }
            case CODE -> {
                if (isBlank(snippet.getContent())) {
                    throw new IllegalArgumentException("CODE 스니펫은 content가 필수입니다.");
                }
                if (isBlank(snippet.getLanguage())) {
                    throw new IllegalArgumentException("CODE 스니펫은 language가 필수입니다.");
                }
            }
            case IMG -> {
                if (isBlank(snippet.getImageUrl())) {
                    throw new IllegalArgumentException("IMG 스니펫은 imageUrl이 필수입니다.");
                }
            }
            default -> throw new IllegalArgumentException("지원하지 않는 스니펫 타입입니다.");
        }
    }

    private void insertDetailByType(SnippetExtCreate snippet) {
        switch (snippet.getType()) {
            case TEXT -> snippetExtMapper.insertSnippetText(snippet);
            case CODE -> snippetExtMapper.insertSnippetCode(snippet);
            case IMG -> snippetExtMapper.insertSnippetImg(snippet);
        }
    }

    private Long getOwnerIdOrThrow(Long snippetId) {
        return snippetExtMapper.findUserIdBySnippetId(snippetId)
            .orElseThrow(() -> new EntityNotFoundException("해당 스니펫이 존재하지 않습니다."));
    }

    private void validateOwnership(Long ownerId, Long requesterId, String message) {
        if (!Objects.equals(requesterId, ownerId)) {
            throw new AccessDeniedException(message);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public List<ColorExt> getColorsByUserId(Long userId) {
        List<ColorExt> colorList = snippetExtMapper.findColorsByUserId(userId);
        return colorList;
    }

    public List<PopSnippets> getTop3PopularSnippets() {
        return snippetExtMapper.selectTop3PopularSnippets();
    }
}
