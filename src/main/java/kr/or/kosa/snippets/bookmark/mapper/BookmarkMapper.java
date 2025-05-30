package kr.or.kosa.snippets.bookmark.mapper;

import kr.or.kosa.snippets.basic.model.Snippets;
import kr.or.kosa.snippets.bookmark.model.Bookmark;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BookmarkMapper {

    // 사용자별 북마크된 스니펫 ID와 기본 정보 + 색상 정보 조회
    @Select("SELECT s.snippet_id, s.user_id, s.folder_id, s.color_id, s.source_url, s.type, " +
            "s.memo, s.created_at, s.updated_at, s.like_count, s.visibility, " +
            "c.name, c.hex_code " +
            "FROM snippets s " +
            "INNER JOIN bookmarks b ON s.snippet_id = b.snippet_id " +
            "LEFT JOIN snippet_colors c ON s.color_id = c.color_id " +
            "WHERE b.user_id = #{userId} ORDER BY s.created_at DESC")
    List<Snippets> selectBookmakredSnippetsByUserId(Long userId);

    //북마크 추가
    @Insert("INSERT INTO bookmarks (user_id, snippet_id) VALUES (#{userId}, #{snippetId})")
    void insertBookmark(@Param("userId") Long userId, @Param("snippetId") Long snippetId);

    //북마크 삭제
    @Delete("DELETE FROM bookmarks WHERE user_id = #{userId} AND snippet_id = #{snippetId}")
    void deleteBookmark(@Param("userId") Long userId, @Param("snippetId") Long snippetId);

    //북마크 여부 확인
    @Select("SELECT COUNT(*) FROM bookmarks WHERE user_id = #{userId} AND snippet_id = #{snippetId}")
    int isBookmarked(@Param("userId") Long userId, @Param("snippetId") Long snippetId);

    // 아래 메서드들은 이제 SnippetService를 통해 처리하므로 제거해도 됩니다
    // 하지만 하위 호환성을 위해 남겨둡니다

    //특정 스니펫 조회 (deprecated - SnippetService 사용 권장)
    @Select("SELECT * FROM snippets WHERE snippet_id = #{snippetId}")
    @Deprecated
    Snippets selectSnippetById(@Param("snippetId") Long snippetId);

    @Select("SELECT * FROM snippets ORDER BY created_at DESC")
    @Deprecated
    List<Snippets> selectAllSnippets();

    // 특정 사용자가 작성한 모든 스니펫 조회 (deprecated - SnippetService 사용 권장)
    @Select("SELECT * FROM snippets WHERE user_id = #{userId} ORDER BY created_at DESC")
    @Deprecated
    List<Snippets> selectSnippetsByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자의 특정 스니펫 북마크 개수 조회 (존재 여부 확인용)
     */
    @Select("SELECT COUNT(*) FROM bookmarks WHERE user_id = #{userId} AND snippet_id = #{snippetId}")
    Integer countBookmark(@Param("userId") Long userId, @Param("snippetId") Long snippetId);

    /**
     * 북마크 추가 (INSERT IGNORE 사용으로 중복 방지)
     */
    @Insert("INSERT IGNORE INTO bookmarks (user_id, snippet_id) VALUES (#{userId}, #{snippetId})")
    int insertBookmarkSafe(@Param("userId") Long userId, @Param("snippetId") Long snippetId);

    /**
     * 북마크 제거
     */
    @Delete("DELETE FROM bookmarks WHERE user_id = #{userId} AND snippet_id = #{snippetId}")
    int deleteBookmarkk(@Param("userId") Long userId, @Param("snippetId") Long snippetId);

    /**
     * 특정 사용자의 모든 북마크 조회
     */
    @Select("SELECT * FROM bookmarks WHERE user_id = #{userId}")
    List<Bookmark> findByUserId(@Param("userId") Long userId);
}