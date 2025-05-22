package kr.or.kosa.snippets.basic.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.kosa.snippets.basic.mapper.SnippetsMapper;
import kr.or.kosa.snippets.basic.model.Snippets;
import kr.or.kosa.snippets.basic.model.SnippetTypeBasic;

@Service
@Transactional
public class SnippetService {

    private final SnippetsMapper snippetsMapper;

    public SnippetService(SnippetsMapper snippetsMapper) {
        this.snippetsMapper = snippetsMapper;
    }

    public List<Snippets> getAllSnippets() {
        return snippetsMapper.getAllSnippets();
    }

    public Snippets getSnippetsById(Long snippetId, SnippetTypeBasic type) {
        return snippetsMapper.getSnippetsById(snippetId, type);
    }

    public SnippetTypeBasic getSnippetTypeById(Long snippetId) {
        return snippetsMapper.getSnippetTypeById(snippetId);
    }

    public void insertSnippets(Snippets snippets) {
        snippetsMapper.insertSnippetBasic(snippets);
        if (snippets.getType() == SnippetTypeBasic.CODE) {
            snippetsMapper.insertSnippetCode(snippets);
        } else if (snippets.getType() == SnippetTypeBasic.TEXT) {
            snippetsMapper.insertSnippetText(snippets);
        } else if (snippets.getType() == SnippetTypeBasic.IMG) {
            snippetsMapper.insertSnippetImage(snippets);
        }
    }

    public void deleteSnippets(Long snippetId) {
        Snippets snippet = snippetsMapper.getSnippetsById(snippetId, null);
        if (snippet == null) {
            throw new RuntimeException("해당 스니펫이 존재하지 않습니다. snippetId=" + snippetId);
        }
        if (snippet.getType() == SnippetTypeBasic.CODE) {
            snippetsMapper.deleteSnippetCodeBySnippetId(snippetId);
        } else if (snippet.getType() == SnippetTypeBasic.TEXT) {
            snippetsMapper.deleteSnippetTextBySnippetId(snippetId);
        } else if (snippet.getType() == SnippetTypeBasic.IMG) {
            snippetsMapper.deleteSnippetImageBySnippetId(snippetId);
        }
        snippetsMapper.deleteSnippets(snippetId);
    }

    public void updateSnippets(Snippets snippet) {
        if (snippet.getType() == SnippetTypeBasic.CODE) {
            snippetsMapper.updateSnippetCode(snippet);
        } else if (snippet.getType() == SnippetTypeBasic.TEXT) {
            snippetsMapper.updateSnippetText(snippet);
        } else if (snippet.getType() == SnippetTypeBasic.IMG) {
            snippetsMapper.updateSnippetImage(snippet);
        }
        snippetsMapper.updateSnippetBasic(snippet);
    }
}