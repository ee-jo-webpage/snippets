package kr.or.kosa.snippets.community.mapper;

import kr.or.kosa.snippets.community.model.BoardCategory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BoardCategoryMapper {
    //매퍼에 필요한 건 : 카테고리 조회(전체조회, 특정ID로 조회), 생성, 수정, 삭제(CRUD)

    List<BoardCategory> getAllCategories();
    BoardCategory getCategoryById(Integer categoryId);
    void insertCategory(BoardCategory category);
    void updateCategory(BoardCategory category);
    void deleteCategory(Integer categoryId);
}
