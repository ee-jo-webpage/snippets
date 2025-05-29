package kr.or.kosa.snippets.community.mapper;

import kr.or.kosa.snippets.community.model.Post;
import kr.or.kosa.snippets.community.model.PostDraft;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PostMapper {
    List<Post> getAllPosts();
    List<Post> getPostsByCategoryId(Integer categoryId);
    List<Post> getPostsByUserId(Long userId);
    Post getPostById(Integer postId);
    void insertPost(Post post);
    int updatePost(Post post);
    void deletePost(Integer postId);
    void increaseViewCount(Integer postId);

    // 검색 기능
    List<Post> searchPosts(@Param("keyword") String keyword,
                           @Param("searchType") String searchType,
                           @Param("categoryId") Integer categoryId,
                           @Param("startDate") LocalDateTime startDate,
                           @Param("endDate") LocalDateTime endDate);
    //임시저장 기능
    List<PostDraft> getDraftsByUserId(Long userId);
    PostDraft getDraftById(Integer draftId);
    void insertDraft(PostDraft draft);
    void updateDraft(PostDraft draft);
    void deleteDraft(Integer draftId);

}
