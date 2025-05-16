package kr.or.kosa.snippets.mapper;

import kr.or.kosa.snippets.model.Bookmark;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

import kr.or.kosa.snippets.model.Bookmark;
import kr.or.kosa.snippets.model.Snippet;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BookmarkMapper {

    // 특정 사용자의 모든 북마크 조회
    @Select("SELECT * FROM bookmarks WHERE user_id = #{userId}")
    List<Bookmark> getBookmarksByUserId(@Param("userId") Long userId);

    // 북마크를 통한 스니펫 조회 (JOIN 사용)
    @Select("""
        SELECT s.*, 
               GROUP_CONCAT(DISTINCT sc.snippet_id, ':', sc.content, ':', sc.language SEPARATOR '||') as code_data,
               GROUP_CONCAT(DISTINCT si.snippet_id, ':', si.image_url, ':', IFNULL(si.alt_text, '') SEPARATOR '||') as image_data,
               GROUP_CONCAT(DISTINCT st.snippet_id, ':', st.content SEPARATOR '||') as text_data
        FROM bookmarks b 
        INNER JOIN snippets s ON b.snippet_id = s.snippet_id
        LEFT JOIN snippet_codes sc ON s.snippet_id = sc.snippet_id
        LEFT JOIN snippet_images si ON s.snippet_id = si.snippet_id  
        LEFT JOIN snippet_texts st ON s.snippet_id = st.snippet_id
        WHERE b.user_id = #{userId}
        GROUP BY s.snippet_id
        ORDER BY s.created_at DESC
    """)
    List<Snippet> getBookmarkedSnippets(@Param("userId") Long userId);

    // 북마크 추가
    @Insert("INSERT INTO bookmarks (user_id, snippet_id) VALUES (#{userId}, #{snippetId})")
    void addBookmark(Bookmark bookmark);

    // 북마크 제거
    @Delete("DELETE FROM bookmarks WHERE user_id = #{userId} AND snippet_id = #{snippetId}")
    void removeBookmark(@Param("userId") Long userId, @Param("snippetId") Long snippetId);

    // 특정 북마크 존재 여부 확인
    @Select("SELECT COUNT(*) FROM bookmarks WHERE user_id = #{userId} AND snippet_id = #{snippetId}")
    int isBookmarked(@Param("userId") Long userId, @Param("snippetId") Long snippetId);
}