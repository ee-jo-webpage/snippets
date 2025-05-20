package kr.or.kosa.snippets.like.mapper;


import kr.or.kosa.snippets.like.model.Like;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LikeMapper {

    // 좋아요 추가
    void addLike(@Param("userId") Integer userId, @Param("snippetId") Integer snippetId);

    // 좋아요 취소
    void removeLike(@Param("userId") Integer userId, @Param("snippetId") Integer snippetId);

    // 특정 스니펫에 대한 좋아요 수 반환
    long countLikes(@Param("snippetId") Integer snippetId);

    // 특정 사용자가 누른 좋아요 목록 반환
    List<Like> getUserLikes(@Param("userId") Integer userId);

    // 사용자가 특정 스니펫에 좋아요를 눌렀는지 확인
    boolean isLiked(@Param("userId") Integer userId, @Param("snippetId") Integer snippetId);
}