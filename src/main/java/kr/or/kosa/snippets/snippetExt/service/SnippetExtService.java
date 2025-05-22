package kr.or.kosa.snippets.snippetExt.service;

import jakarta.persistence.EntityNotFoundException;
import kr.or.kosa.snippets.snippetExt.mapper.SnippetExtMapper;
import kr.or.kosa.snippets.snippetExt.model.SnippetExtCreate;
import kr.or.kosa.snippets.snippetExt.model.SnippetExtUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class SnippetExtService {

    private final SnippetExtMapper snippetExtMapper;

    @Transactional
    public Long save(SnippetExtCreate snippet) {
        snippetExtMapper.insertSnippet(snippet); // snippets 테이블

        switch (snippet.getType()) {
            case TEXT -> snippetExtMapper.insertSnippetText(snippet);
            case CODE -> snippetExtMapper.insertSnippetCode(snippet);
            case IMG -> snippetExtMapper.insertSnippetImg(snippet);
        }

        return snippet.getSnippetId();
    }

    public void updateSnippet(SnippetExtUpdate snippetUpdate, Long requesterId) {
        Long ownerId = snippetExtMapper.findUserIdBySnippetId(snippetUpdate.getSnippetId())
            .orElseThrow(() -> new EntityNotFoundException("해당 스니펫이 존재하지 않습니다."));

        if (!Objects.equals(requesterId, ownerId)) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }
        snippetUpdate.setUserId(requesterId);
        snippetExtMapper.updateSnippet(snippetUpdate);
    }

    public void deleteSnippet(Long snippetId, Long requesterId) {
        Long ownerId = snippetExtMapper.findUserIdBySnippetId(snippetId)
            .orElseThrow(() -> new EntityNotFoundException("삭제할 스니펫이 존재하지 않습니다."));

        if (!Objects.equals(requesterId, ownerId)) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }

        snippetExtMapper.deleteSnippet(snippetId);
    }
}
