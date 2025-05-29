package kr.or.kosa.snippets.snippetExt.mapper;

import kr.or.kosa.snippets.snippetExt.model.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface SnippetExtMapper {

    void insertSnippet(SnippetExtCreate snippetExt);

    void insertSnippetText(SnippetExtCreate snippetExt);

    void insertSnippetCode(SnippetExtCreate snippet);

    void updateSnippet(SnippetExtUpdate snippetUpdate);

    void insertSnippetImg(SnippetExtCreate snippet);

    void deleteSnippet(Long id);

    Optional<Long> findUserIdBySnippetId(Long snippetId);

    int countDuplicate(SnippetExtCreate snippet);

    List<ColorExt> findColorsByUserId(Long userId);

    List<PopSnippets> selectTop3PopularSnippets();

    List<SnippetMapping> findSnippetIdsByClientRequestIds(@Param("list") List<String> list);

    void bulkInsertSnippetTexts(List<SnippetExtCreate> list);

    void bulkInsertSnippetCodes(List<SnippetExtCreate> list);

    void bulkInsertSnippetImages(List<SnippetExtCreate> list);

}
