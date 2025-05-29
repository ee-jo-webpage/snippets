package kr.or.kosa.snippets.basic.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.kosa.snippets.like.model.SnippetCode;
import kr.or.kosa.snippets.like.model.SnippetImage;
import kr.or.kosa.snippets.like.model.SnippetText;

@Mapper
public interface SnippetsContentCrud {

	// 코드 스니펫 내용 조회
    SnippetCode getSnippetCodeById(@Param("snippetId") Long snippetId);

    // 텍스트 스니펫 내용 조회
    SnippetText getSnippetTextById(@Param("snippetId") Long snippetId);

    // 이미지 스니펫 내용 조회
    SnippetImage getSnippetImageById(@Param("snippetId") Long snippetId);
	
}
