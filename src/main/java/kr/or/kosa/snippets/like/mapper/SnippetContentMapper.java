package kr.or.kosa.snippets.like.mapper;

import kr.or.kosa.snippets.like.model.SnippetCode;
import kr.or.kosa.snippets.like.model.SnippetText;
import kr.or.kosa.snippets.like.model.SnippetImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SnippetContentMapper {

    // 코드 스니펫 내용 조회
    SnippetCode getSnippetCodeById(@Param("snippetId") Long snippetId);

    // 텍스트 스니펫 내용 조회
    SnippetText getSnippetTextById(@Param("snippetId") Long snippetId);

    // 이미지 스니펫 내용 조회
    SnippetImage getSnippetImageById(@Param("snippetId") Long snippetId);
}