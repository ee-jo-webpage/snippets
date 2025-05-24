package kr.or.kosa.snippets.tag.mapper;

import kr.or.kosa.snippets.basic.model.Snippets;
import kr.or.kosa.snippets.tag.model.SnippetTag;
import kr.or.kosa.snippets.tag.model.TagItem;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository("tagMapperBean")
public interface TagMapper {

    // 사용자 ID로 모든 태그 조회
    @Select("SELECT * FROM tags WHERE user_id = #{userId} ORDER BY name")
    List<TagItem> selectTagsByUserId(Long userId);

    // 사용자 ID와 태그 이름으로 태그 조회
    @Select("SELECT * FROM tags WHERE user_id = #{userId} AND name = #{name}")
    TagItem selectTagByUserIdAndName(@Param("userId") Long userId, @Param("name") String name);

    // 태그 ID로 태그 조회
    @Select("SELECT * FROM tags WHERE tag_id = #{tagId}")
    TagItem selectTagById(@Param("tagId") Long tagId);

    // 사용자별 태그 생성 (user_id 포함)
    @Insert("INSERT INTO tags (user_id, name) VALUES (#{userId}, #{name})")
    @Options(useGeneratedKeys = true, keyProperty = "tagId")
    int insertTag(@Param("userId") Long userId, @Param("name") String name);

    // 특정 스니펫의 태그 조회
    @Select("SELECT t.* FROM tags t INNER JOIN snippet_tags st ON t.tag_id = st.tag_id WHERE st.snippet_id = #{snippetId}")
    List<TagItem> selectTagBySnippetId(Long snippetId);

    // 스니펫-태그 연결 여부 확인
    @Select("SELECT COUNT(*) FROM snippet_tags WHERE snippet_id = #{snippetId} AND tag_id = #{tagId}")
    int countSnippetTag(@Param("snippetId") Long snippetId, @Param("tagId") Long tagId);

    // 스니펫-태그 연결
    @Insert("INSERT INTO snippet_tags (snippet_id, tag_id) VALUES (#{snippetId}, #{tagId})")
    int insertSnippetTag(SnippetTag snippetTag);

    // 태그 삭제
    @Delete("DELETE FROM tags WHERE tag_id = #{tagId} AND user_id = #{userId}")
    int deleteTag(@Param("tagId") Long tagId, @Param("userId") Long userId);

    // 스니펫-태그 연결 제거
    @Delete("DELETE FROM snippet_tags WHERE snippet_id = #{snippetId} AND tag_id = #{tagId}")
    int deleteSnippetTag(@Param("snippetId") Long snippetId, @Param("tagId") Long tagId);

    // 태그ID로 모든 스니펫-태그 연결 제거
    @Delete("DELETE FROM snippet_tags WHERE tag_id = #{tagId}")
    int deleteSnippetTagsByTagId(Long tagId);

    // 특정 스니펫 조회
    @Select("SELECT * FROM snippets WHERE snippet_id = #{snippetId}")
    Snippets selectSnippetById(@Param("snippetId") Long snippetId);

    // 모든 스니펫 조회
    @Select("SELECT * FROM snippets ORDER BY created_at DESC")
    List<Snippets> selectAllSnippets();

    // 특정 사용자가 작성한 모든 스니펫 조회
    @Select("SELECT * FROM snippets WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Snippets> selectSnippetsByUserId(@Param("userId") Long userId);

    // 특정 태그가 연결된 스니펫 수 조회
    @Select("SELECT COUNT(*) FROM snippet_tags WHERE tag_id = #{tagId}")
    int countSnippetsByTagId(@Param("tagId") Long tagId);

    // 사용자가 가장 많이 사용한 태그 조회 (상위 N개)
    @Select("SELECT t.*, COUNT(st.tag_id) as usage_count " +
            "FROM tags t " +
            "LEFT JOIN snippet_tags st ON t.tag_id = st.tag_id " +
            "WHERE t.user_id = #{userId} " +
            "GROUP BY t.tag_id, t.user_id, t.name " +
            "ORDER BY usage_count DESC " +
            "LIMIT #{limit}")
    List<TagItem> selectMostUsedTagsByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    // 특정 태그로 스니펫 검색
    @Select("SELECT s.* FROM snippets s " +
            "INNER JOIN snippet_tags st ON s.snippet_id = st.snippet_id " +
            "INNER JOIN tags t ON st.tag_id = t.tag_id " +
            "WHERE t.tag_id = #{tagId} " +
            "ORDER BY s.created_at DESC")
    List<Snippets> selectSnippetsByTagId(@Param("tagId") Long tagId);

    // 여러 태그로 스니펫 검색 (AND 조건)
    @Select("<script>" +
            "SELECT s.* FROM snippets s " +
            "WHERE s.snippet_id IN (" +
            "  SELECT st.snippet_id FROM snippet_tags st " +
            "  WHERE st.tag_id IN " +
            "  <foreach collection='tagIds' item='tagId' open='(' separator=',' close=')'>" +
            "    #{tagId}" +
            "  </foreach>" +
            "  GROUP BY st.snippet_id " +
            "  HAVING COUNT(DISTINCT st.tag_id) = #{tagIds.size()}" +
            ") " +
            "ORDER BY s.created_at DESC" +
            "</script>")
    List<Snippets> selectSnippetsByMultipleTags(@Param("tagIds") List<Long> tagIds);

    // 태그명으로 검색 (사용자별)
    @Select("SELECT * FROM tags WHERE user_id = #{userId} AND name LIKE CONCAT('%', #{keyword}, '%') ORDER BY name")
    List<TagItem> searchTagsByKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);
}