package kr.or.kosa.snippets.snippetExt.mapper;

import kr.or.kosa.snippets.snippetExt.model.*;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
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

    int countDuplicate(SnippetExtCreate snippet);

    List<ColorExt> findColorsByUserId(Long userId);

    List<PopSnippets> selectTop3PopularSnippets();

    // 기존 insertSnippet 로 대체
    // void bulkInsertSnippets(List<SnippetExtCreate> snippets);

    // 중복검사 로직 삭제로 인한 미사용
    // Long findIdByClientRequestId(String clientRequestId);

    List<SnippetMapping> findSnippetIdsByClientRequestIds(@Param("list") List<String> list);

    void bulkInsertSnippetTexts(List<SnippetExtCreate> list);

    void bulkInsertSnippetCodes(List<SnippetExtCreate> list);

    void bulkInsertSnippetImages(List<SnippetExtCreate> list);

    // 중복검사 로직 삭제로 인한 미사용
    // List<String> findDuplicateSnippets(@Param("snippets") List<SnippetExtCreate> snippets);

}
