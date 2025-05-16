package kr.or.kosa.snippets.mapper;

import kr.or.kosa.snippets.model.SnippetCode;
import kr.or.kosa.snippets.model.SnippetText;
import kr.or.kosa.snippets.model.SnippetImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SnippetContentMapper {

    // 코드 스니펫 내용 조회
    SnippetCode getSnippetCodeById(@Param("snippetId") Integer snippetId);

    // 텍스트 스니펫 내용 조회
    SnippetText getSnippetTextById(@Param("snippetId") Integer snippetId);

    // 이미지 스니펫 내용 조회
    SnippetImage getSnippetImageById(@Param("snippetId") Integer snippetId);
}