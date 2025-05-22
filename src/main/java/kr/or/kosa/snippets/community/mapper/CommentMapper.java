package kr.or.kosa.snippets.community.mapper;

import kr.or.kosa.snippets.community.model.Comment;
import kr.or.kosa.snippets.community.model.CommentLike;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {
    List<Comment> getCommentsByPostId(Integer postId);
    Comment getCommentById(Integer commentId);
    void insertComment(Comment comment);
    void updateComment(Comment comment);
    void deleteComment(Integer commentId);

    //대댓글 관련
    List<Comment> getChildComments(Integer parentId);

    //좋아요/싫어요 관련
    void insertCommentLike(CommentLike commentLike);
    void updateCommentLike(CommentLike commentLike);
    void deleteCommentLike(Integer commentLikeId);
    CommentLike getCommentLike(Integer commentId, Long userId);
    Integer getLikeCount(Integer commentId, boolean isLike);
}
