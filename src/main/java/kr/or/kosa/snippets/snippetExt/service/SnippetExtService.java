package kr.or.kosa.snippets.snippetExt.service;

import jakarta.persistence.EntityNotFoundException;
import kr.or.kosa.snippets.snippetExt.exception.DuplicateSnippetException;
import kr.or.kosa.snippets.snippetExt.mapper.SnippetExtMapper;
import kr.or.kosa.snippets.snippetExt.model.*;
import kr.or.kosa.snippets.snippetExt.type.SnippetType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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

    // @Transactional
    // public void saveAll(List<SnippetExtCreate> snippets) {
    //     log.warn("[1] 스니펫 저장 요청: 총 {}건", snippets == null ? 0 : snippets.size());
    //     if (snippets == null || snippets.isEmpty()) return;
    //
    //     // 1. 유효성 검사 (userId, type, 내용 필수 여부 검사)
    //     List<SnippetExtCreate> validSnippets = snippets.stream()
    //         .filter(this::isValid)
    //         .toList();
    //
    //     log.warn("[2] 유효성 통과 스니펫 수: {}", validSnippets.size());
    //     if (validSnippets.isEmpty()) return;
    //
    //     try {
    //         // 2. 메타 테이블 batch insert
    //         for (SnippetExtCreate snippet : validSnippets) {
    //             snippetExtMapper.insertSnippet(snippet);
    //         }
    //         log.warn("[3] insertSnippet 반복 후 flush 완료");
    //
    //         // 3. clientRequestId 기반으로 snippetId 매핑 조회
    //         List<String> clientIds = validSnippets.stream()
    //             .map(SnippetExtCreate::getClientRequestId)
    //             .filter(Objects::nonNull)
    //             .toList();
    //
    //         List<SnippetMapping> mappings = snippetExtMapper.findSnippetIdsByClientRequestIds(clientIds);
    //
    //         Map<String, Long> idMap = mappings.stream()
    //             .collect(Collectors.toMap(SnippetMapping::getClientRequestId, SnippetMapping::getSnippetId));
    //
    //         log.warn("[4] 매핑된 snippetId 수: {}", idMap.size());
    //         idMap.forEach((key, value) -> log.warn("🔑 매핑: {} → {}", key, value));
    //
    //         // 4. 매핑 실패 필터링 + snippetId 세팅
    //         List<SnippetExtCreate> mappedSnippets = validSnippets.stream()
    //             .filter(s -> {
    //                 Long id = idMap.get(s.getClientRequestId());
    //                 if (id == null) {
    //                     log.error("❗ [5] snippetId 매핑 실패 → clientRequestId: {}", s.getClientRequestId());
    //                     return false;
    //                 }
    //                 s.setSnippetId(id);
    //                 return true;
    //             })
    //             .toList();
    //
    //         // 5. 타입별로 분기
    //         List<SnippetExtCreate> texts = mappedSnippets.stream()
    //             .filter(s -> SnippetType.TEXT.equals(s.getType()))
    //             .toList();
    //         List<SnippetExtCreate> codes = mappedSnippets.stream()
    //             .filter(s -> SnippetType.CODE.equals(s.getType()))
    //             .toList();
    //         List<SnippetExtCreate> imgs = mappedSnippets.stream()
    //             .filter(s -> SnippetType.IMG.equals(s.getType()))
    //             .toList();
    //
    //         log.warn("[6] TEXT 타입 스니펫 수: {}", texts.size());
    //         log.warn("[7] CODE 타입 스니펫 수: {}", codes.size());
    //         log.warn("[8] IMG 타입 스니펫 수: {}", imgs.size());
    //
    //         // 6. 하위 테이블 일괄 insert
    //         if (!texts.isEmpty()) snippetExtMapper.bulkInsertSnippetTexts(texts);
    //         if (!codes.isEmpty()) snippetExtMapper.bulkInsertSnippetCodes(codes);
    //         if (!imgs.isEmpty()) snippetExtMapper.bulkInsertSnippetImages(imgs);
    //
    //     } catch (org.springframework.dao.DuplicateKeyException e) {
    //         // clientRequestId 중복 등록 방지
    //         throw new DuplicateSnippetException("이미 등록된 clientRequestId가 포함되어 있습니다.");
    //     }
    // }

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

    public List<ColorExt> getColorsByUserId(Long userId) {
        return snippetExtMapper.findColorsByUserId(userId);
    }

    public List<PopSnippets> getTop3PopularSnippets() {
        return snippetExtMapper.selectTop3PopularSnippets();
    }

    // ================================================
    // private 내부 로직
    // ================================================

    // 동기식 insert - save
    private boolean isDuplicate(SnippetExtCreate snippet) {
        return snippetExtMapper.countDuplicate(snippet) > 0;
    }

    // 동기식 insert - save
    private void validateSnippet(SnippetExtCreate snippet) {
        if (!isValid(snippet)) {
            throw new IllegalArgumentException("스니펫 유효성 검사 실패: " + snippet);
        }
    }

    private boolean isValid(SnippetExtCreate snippet) {
        if (snippet.getUserId() == null || snippet.getType() == null) return false;

        return switch (snippet.getType()) {
            case TEXT -> !isBlank(snippet.getContent());
            case CODE -> !isBlank(snippet.getContent()) && !isBlank(snippet.getLanguage());
            case IMG -> !isBlank(snippet.getImageUrl());
        };
    }

    // 동기식 insert - save
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

}
