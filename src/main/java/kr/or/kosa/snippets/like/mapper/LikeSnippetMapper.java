package kr.or.kosa.snippets.like.mapper;

import kr.or.kosa.snippets.like.model.Snippet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LikeSnippetMapper {

    // 특정 사용자의 스니펫 목록 조회
    List<Snippet> getSnippetsByUserId(@Param("userId") Long userId);

    // 모든 공개 스니펫 목록 조회
    List<Snippet> getAllPublicSnippets();

    // 인기 스니펫 조회 (기존 방식)
    List<Snippet> getPopularSnippets(@Param("limit") Integer limit);

    // 뷰를 이용한 인기 스니펫 조회 (새로 추가)
    List<Snippet> getPopularSnippetsFromView(@Param("limit") Integer limit);

    // 뷰를 이용한 상위 100개 인기 스니펫 조회 (새로 추가)
    List<Snippet> getTop100PopularSnippets();

    // 검색 조건에 따른 스니펫 조회
    List<Snippet> searchSnippets(@Param("type") String type,
                                 @Param("keyword") String keyword,
                                 @Param("minLikes") Integer minLikes,
                                 @Param("tagName") String tagName);

    // 특정 스니펫 조회
    Snippet getSnippetById(@Param("snippetId") Long snippetId);

    // 스니펫 상세 정보 조회 (태그 포함)
    Snippet getSnippetDetailById(@Param("snippetId") Long snippetId);

    // 좋아요 수 업데이트
    void updateLikeCount(@Param("snippetId") Long snippetId, @Param("likeCount") Integer likeCount);

    // 페이징 처리된 인기 스니펫 조회 (기존)
    List<Snippet> getPopularSnippetsPaged(@Param("offset") Integer offset, @Param("limit") Integer limit);

    // 뷰를 이용한 페이징 처리된 인기 스니펫 조회 (새로 추가)
    List<Snippet> getPopularSnippetsPagedFromView(@Param("offset") Integer offset, @Param("limit") Integer limit);

    // 페이징 처리된 최신순 스니펫 조회
    List<Snippet> getAllPublicSnippetsPaged(@Param("offset") Integer offset, @Param("limit") Integer limit);

    // 전체 공개 스니펫 수 조회
    int countAllPublicSnippets();

    // 뷰에서 전체 인기 스니펫 수 조회 (새로 추가)
    int countPopularSnippetsFromView();

}