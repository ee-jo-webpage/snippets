package kr.or.kosa.snippets.basic.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.kosa.snippets.basic.model.Snippets;
import kr.or.kosa.snippets.basic.model.SnippetTypeBasic;

@Mapper
public interface SnippetsMapper {
    // 전체보기
    List<Snippets> getAllSnippets();

    // 상세보기
    Snippets getSnippetsById(@Param("snippetId") Long snippetId, @Param("type") SnippetTypeBasic type);
    
    
    // 특정 유저의 게시글만 보기
    List<Snippets> getUserSnippets(Long userId);
    


    // 기본 스니펫 등록 (snippets 테이블)
    void insertSnippetBasic(Snippets snippets);
    // 코드 스니펫 등록 (snippet_codes 테이블)
    void insertSnippetCode(Snippets snippets);
    // 텍스트 스니펫 등록 (snippet_texts 테이블)
    void insertSnippetText(Snippets snippets);
    // 이미지 스니펫 등록 (snippet_images 테이블)
    void insertSnippetImage(Snippets snippets);

    
    // 게시글 수정
    void updateSnippetBasic(Snippets snippets);
    // 코드 정보 수정
    void updateSnippetCode(Snippets snippets);
    // 텍스트 정보 수정
    void updateSnippetText(Snippets snippets);
    // 이미지 정보 수정
    void updateSnippetImage(Snippets snippets);

    
    // 코드 게시글 삭제
    void deleteSnippetCodeBySnippetId(@Param("snippetId") Long snippetId);
    // 텍스트 게시글 삭제
    void deleteSnippetTextBySnippetId(@Param("snippetId") Long snippetId);
    // 이미지 게시글 삭제
    void deleteSnippetImageBySnippetId(@Param("snippetId") Long snippetId);
    // 게시글 삭제
    void deleteSnippets(@Param("snippetId") Long snippetId);
    // 추가: snippets에서 type 조회 (getSnippetsById에서 type이 null일 때 사용)
    // 수정: @Param 을 붙여서 이름을 명시
    SnippetTypeBasic getSnippetTypeById(@Param("snippetId") Long snippetId);

    
    
}
