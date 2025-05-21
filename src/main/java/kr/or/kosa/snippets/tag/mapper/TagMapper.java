package kr.or.kosa.snippets.tag.mapper;

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
}