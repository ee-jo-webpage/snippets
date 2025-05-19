package kr.or.kosa.snippets.snippetExt.service;

import kr.or.kosa.snippets.snippetExt.mapper.SnippetExtMapper;
import kr.or.kosa.snippets.snippetExt.model.SnippetExtCreate;
import kr.or.kosa.snippets.snippetExt.model.SnippetExtUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SnippetService {

    private final SnippetExtMapper snippetExtMapper;

    @Transactional
    public Long save(SnippetExtCreate snippet) {
        snippetExtMapper.insertSnippet(snippet); // snippets 테이블

        // 2. 스니펫 유형에 따라 분기 저장
        switch (snippet.getType()) {
            case TEXT -> snippetExtMapper.insertSnippetText(snippet);
            case CODE -> snippetExtMapper.insertSnippetCode(snippet);
        }

        return snippet.getSnippetId();
    }

    public void updateSnippet(SnippetExtUpdate snippetUpdate) {
        snippetExtMapper.updateSnippet(snippetUpdate);
    }
}