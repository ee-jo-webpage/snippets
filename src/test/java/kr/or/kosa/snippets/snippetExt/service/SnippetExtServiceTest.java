package kr.or.kosa.snippets.snippetExt.service;

import kr.or.kosa.snippets.snippetExt.exception.DuplicateSnippetException;
import kr.or.kosa.snippets.snippetExt.mapper.SnippetExtMapper;
import kr.or.kosa.snippets.snippetExt.model.SnippetExtCreate;
import kr.or.kosa.snippets.snippetExt.type.SnippetType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SnippetExtServiceTest {

    private SnippetExtMapper mapper;
    private SnippetExtService service;

    @BeforeEach
    void setUp() {
        mapper = mock(SnippetExtMapper.class);
        service = new SnippetExtService(mapper);
    }

    @Test
    void 중복이_아니면_정상적으로_저장된다() {
        // given
        SnippetExtCreate snippet = new SnippetExtCreate();
        snippet.setUserId(1L);
        snippet.setType(SnippetType.TEXT);
        snippet.setContent("Hello");
        snippet.setSourceUrl("https://example.com");

        when(mapper.countDuplicate(snippet)).thenReturn(0);

        // when
        service.save(snippet);

        // then
        verify(mapper).insertSnippet(snippet);       // 저장 호출됨
        verify(mapper).insertSnippetText(snippet);   // TEXT용 저장 호출됨
    }

    @Test
    void 중복이면_예외를_던진다() {
        // given
        SnippetExtCreate snippet = new SnippetExtCreate();
        snippet.setUserId(1L);
        snippet.setType(SnippetType.TEXT);
        snippet.setContent("Hello");
        snippet.setSourceUrl("https://example.com");

        when(mapper.countDuplicate(snippet)).thenReturn(1);

        // when & then
        assertThrows(DuplicateSnippetException.class, () -> service.save(snippet));

        verify(mapper, never()).insertSnippet(any());
        verify(mapper, never()).insertSnippetText(any());
    }
}
