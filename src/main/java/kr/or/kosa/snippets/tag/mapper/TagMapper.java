package kr.or.kosa.snippets.tag.mapper;

import kr.or.kosa.snippets.basic.model.Snippets;
import kr.or.kosa.snippets.tag.model.SnippetTag;
import kr.or.kosa.snippets.tag.model.TagItem;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository("tagMapperBean")
public interface TagMapper {

    //임시 - 이후 삭제할 것
    @Select("SELECT * FROM snippets WHERE user_id = #{userId}")
    List<Snippets> selectAllSnippetsByUserId(Long userId);

    //모든 태그 조회
    @Select("SELECT * FROM tags ORDER BY name")
    List<TagItem> selectAllTags();

    //태그이름으로 태그 조회
    @Select("SELECT * FROM tags WHERE name =#{name}")
    TagItem selectTagByName(String name);

    //태그 ID로 태그 조회
    @Select("SELECT * FROM tags WHERE tag_id = #{tagId}")
    TagItem selectTagById(@Param("tagId") Long tagId);

    //태그 생성
    @Insert("INSERT INTO tags (name) VALUES (#{name})")
    @Options(useGeneratedKeys = true, keyProperty = "tagId")
    int insertTag(TagItem tag);

    //태그로 스니펫 검색
    @Select("SELECT t.* FROM tags t INNER JOIN snippet_tags st ON t.tag_id=st.tag_id WHERE st.snippet_id = #{snippetId}")
    List<TagItem> selectTagBySnippetId(Long snippetId);

    @Select("SELECT COUNT(*) FROM snippet_tags WHERE snippet_id = #{snippetId} AND tag_id = #{tagId}")
    int countSnippetTag(@Param("snippetId") Long snippetId, @Param("tagId") Long tagId);

    @Insert("INSERT INTO snippet_tags (snippet_id, tag_id) VALUES (#{snippetId}, #{tagId})")
    int insertSnippetTag(SnippetTag snippetTag);

    //태그 제거
    @Delete("DELETE FROM tags WHERE tag_id = #{tagId}")
    void deleteTag(Long tagId);

    //스니펫 태그 제거
    @Delete("DELETE FROM snippet_tags WHERE snippet_id = #{snippetId} AND tag_id = #{tagId}")
    int deleteSnippetTag(@Param("snippetId") Long snippetId, @Param("tagId") Long tagId);

    //태그ID로 태그 제거
    @Delete("DELETE FROM snippet_tags WHERE tag_id = #{tagId}")
    int deleteSnippetTagsByTagId(Long tagId);



}