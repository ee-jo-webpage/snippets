package kr.or.kosa.snippets.mapper;

import kr.or.kosa.snippets.model.Snippet;
import kr.or.kosa.snippets.model.SnippetTag;
import kr.or.kosa.snippets.model.Tag;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface TagMapper {

    //태그 CRUD

    List<Map<String, Object>> findSnippetsByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM tags ORDER BY name")
    List<Tag> selectAllTags();

    List<Tag> selectTagsByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM tags WHERE tag_id = #{tagId}")
    Tag selectTagById(@Param("tagId") Long tagId);

    @Select("SELECT * FROM tags WHERE name =#{name}")
    Tag selectTagByName(String name);

    List<Map<String, Object>> findSnippetsByUserIdAndTagId(
            @Param("userId") Long userId,
            @Param("tagId") Long tagId
    );

    @Insert("INSERT INTO tags (name) VALUES (#{name})")
    @Options(useGeneratedKeys = true, keyProperty = "tagId")
    int insertTag(Tag tag);

    @Update("UPDATE tags SET name = #{name} WHERE tag_id = #{tagId}")
    int updateTag(Tag tag);

    @Delete("DELETE FROM tags WHERE tag_id = #{tagId}")
    void deleteTag(Long tagId);

    // 스니펫-태그 관계 CRUD

    @Insert("INSERT INTO snippet_tags (snippet_id, tag_id) VALUES (#{snippetId}, #{tagId})")
    int insertSnippetTag(SnippetTag snippetTag);

    @Delete("DELETE FROM snippet_tags WHERE snippet_id = #{snippetId} AND tag_id = #{tagId}")
    int deleteSnippetTag(@Param("snippetId") Long snippetId, @Param("tagId") Long tagId);

    @Delete("DELETE FROM snippet_tags WHERE tag_id = #{tagId}")
    int deleteSnippetTagsByTagId(Long tagId);

    @Select("SELECT COUNT(*) FROM snippet_tags WHERE snippet_id = #{snippetId} AND tag_id = #{tagId}")
    int countSnippetTag(@Param("snippetId") Long snippetId, @Param("tagId") Long tagId);

    //태그로 스니펫 검색
    @Select("SELECT t.* FROM tags t INNER JOIN snippet_tags st ON t.tag_id=st.tag_id WHERE st.snippet_id = #{snippetId}")
    List<Tag> selectTagBySnippetId(Long snippetId);

    //특정 태그를 가진 스니펫 ID목록
    @Select("SELECT * FROM snippet_tags WHERE tag_id = #{tagId}")
    List<Snippet> selectSnippetIdByTagId(Long tagId);

    // 태그별 스니펫 조회
    @Select("SELECT s.snippet_id, s.memo as title, s.type, s.created_at " +
            "FROM snippets s " +
            "INNER JOIN snippet_tags st ON s.snippet_id = st.snippet_id " +
            "WHERE st.tag_id = #{tagId} " +
            "ORDER BY s.created_at DESC")
    List<Snippet> selectSnippetsByTagId(@Param("tagId") Long tagId);
}