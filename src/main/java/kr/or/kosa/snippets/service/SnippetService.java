package kr.or.kosa.snippets.service;

import jakarta.transaction.Transactional;
import kr.or.kosa.snippets.mapper.SnippetMapper;
import kr.or.kosa.snippets.model.Snippet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class SnippetService {

    @Autowired
    private SnippetMapper snippetMapper;

    public List<Snippet> getSnippetsByTagId(Long tagId) {
        return snippetMapper.findSnippetsByTagId(tagId);
    }

    public Snippet getSnippetById(Long id) {
        return snippetMapper.findById(id);
    }

    public List<Snippet> getAllSnippets() {
        return snippetMapper.findAll();
    }

    public Snippet createSnippet(Snippet snippet) {
        snippetMapper.insertSnippet(snippet);
        return snippet;
    }

    public void updateSnippet(Snippet snippet) {
        snippetMapper.updateSnippet(snippet);
    }

    public void deleteSnippet(Long id) {
        snippetMapper.deleteSnippet(id);
    }
}
