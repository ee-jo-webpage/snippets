package kr.or.kosa.snippets.mapper;

import kr.or.kosa.snippets.model.Snippet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SnippetMapper {
    List<Snippet> findSnippetsByTagId(@Param("tagId") Long tagId);
    Snippet findById(@Param("id") Long id);
    List<Snippet> findAll();
    void insertSnippet(Snippet snippet);
    void updateSnippet(Snippet snippet);
    void deleteSnippet(@Param("id") Long id);
}
