package kr.or.kosa.snippets.bookmark.mapper;

import kr.or.kosa.snippets.basic.model.Snippets;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BookmarkMapper {

    //사용자별 북마크된 스니펫 조회
    @Select("SELECT s.* FROM snippets s INNER JOIN bookmarks b ON s.snippet_id = b.snippet_id WHERE b.user_id = #{userId}")
    List<Snippets> selectBookmakredSnippetsByUserId(Long userId);
}
