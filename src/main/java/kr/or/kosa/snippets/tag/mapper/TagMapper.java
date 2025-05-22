package kr.or.kosa.snippets.tag.mapper;

import kr.or.kosa.snippets.tag.model.SnippetTag;
import kr.or.kosa.snippets.tag.model.TagItem;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository("tagMapperBean")
public interface TagMapper {

    @Select("SELECT * FROM tags ORDER BY name")
    List<TagItem> selectAllTags();

    @Select("SELECT * FROM tags WHERE name =#{name}")
    TagItem selectTagByName(String name);

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
}