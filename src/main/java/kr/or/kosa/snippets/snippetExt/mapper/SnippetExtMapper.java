package kr.or.kosa.snippets.snippetExt.mapper;

import kr.or.kosa.snippets.snippetExt.model.SnippetExtCreate;
import kr.or.kosa.snippets.snippetExt.model.SnippetExtUpdate;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface SnippetExtMapper {

    void insertSnippet(SnippetExtCreate snippetExt);

    void insertSnippetText(SnippetExtCreate snippetExt);

    void updateSnippet(SnippetExtUpdate snippetUpdate);

    void insertSnippetCode(SnippetExtCreate snippet);

    void deleteSnippet(Long id);

    void insertSnippetImg(SnippetExtCreate snippet);

    Optional<Long> findUserIdBySnippetId(Long snippetId);
}
