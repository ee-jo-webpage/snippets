package kr.or.kosa.snippets.bookmark.mapper;

import kr.or.kosa.snippets.basic.model.Snippets;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BookmarkMapper {

    //사용자별 북마크된 스니펫 조회
    @Select("SELECT s.* FROM snippets s INNER JOIN bookmarks b ON s.snippet_id = b.snippet_id WHERE b.user_id = #{userId}")
    List<Snippets> selectBookmakredSnippetsByUserId(Long userId);

    //북마크 추가
    @Insert("INSERT INTO bookmarks (user_id, snippet_id) VALUES (#{userId}, #{snippetId})")
    void insertBookmark(@Param("userId") Long userId, @Param("snippetId") Long snippetId);

    //북마크 삭제 (추가)
    @Delete("DELETE FROM bookmarks WHERE user_id = #{userId} AND snippet_id = #{snippetId}")
    void deleteBookmark(@Param("userId") Long userId, @Param("snippetId") Long snippetId);

    //북마크 여부 확인 (추가)
    @Select("SELECT COUNT(*) FROM bookmarks WHERE user_id = #{userId} AND snippet_id = #{snippetId}")
    int isBookmarked(@Param("userId") Long userId, @Param("snippetId") Long snippetId);

    //특정 스니펫 조회 (스니펫 서비스 대신 사용) (추가)
    @Select("SELECT * FROM snippets WHERE snippet_id = #{snippetId}")
    Snippets selectSnippetById(@Param("snippetId") Long snippetId);

    @Select("SELECT * FROM snippets ORDER BY created_at DESC")
    List<Snippets> selectAllSnippets();

    // 특정 사용자가 작성한 모든 스니펫 조회 (추가)
    @Select("SELECT * FROM snippets WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Snippets> selectSnippetsByUserId(@Param("userId") Long userId);
}