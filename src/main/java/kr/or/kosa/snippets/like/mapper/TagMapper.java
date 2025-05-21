package kr.or.kosa.snippets.like.mapper;

import kr.or.kosa.snippets.like.model.LikeTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TagMapper {

    // 모든 태그 조회
    List<LikeTag> getAllTags();

    // 특정 스니펫의 태그 조회
    List<LikeTag> getTagsBySnippetId(@Param("snippetId") Integer snippetId);
}