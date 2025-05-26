package kr.or.kosa.snippets.community.service;

import kr.or.kosa.snippets.community.mapper.CommentMapper;
import kr.or.kosa.snippets.community.mapper.PostMapper;
import kr.or.kosa.snippets.community.model.Comment;
import kr.or.kosa.snippets.community.model.CommentLike;
import kr.or.kosa.snippets.community.model.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {
    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final NotificationService notificationService;

    public List<Comment> getCommentsByPostId(Integer postId, Long currentUserId) {
        List<Comment> comments = commentMapper.getCommentsByPostId(postId);

        // 현재 사용자의 좋아요 상태 설정
        for (Comment comment : comments) {
            if (currentUserId != null) {
                CommentLike like = commentMapper.getCommentLike(comment.getCommentId(), currentUserId);
                if (like != null) {
                    comment.setIsLiked(like.isLike());
                    comment.setIsDisliked(!like.isLike());
                }
            }

            // 대댓글이 없는 부모 댓글인 경우 대댓글 목록 조회
            if (comment.getParentId() == null) {
                List<Comment> childComments = commentMapper.getChildComments(comment.getCommentId());

                // 대댓글의 좋아요 상태 설정
                for (Comment childComment : childComments) {
                    if (currentUserId != null) {
                        CommentLike childLike = commentMapper.getCommentLike(childComment.getCommentId(), currentUserId);
                        if (childLike != null) {
                            childComment.setIsLiked(childLike.isLike());
                            childComment.setIsDisliked(!childLike.isLike());
                        }
                    }
                }

                comment.setChildComments(childComments);
            }
        }

        return comments;
    }

    public Comment getCommentById(Integer commentId) {
        return commentMapper.getCommentById(commentId);
    }

    public Integer addComment(Comment comment) {
        commentMapper.insertComment(comment);

        // 알림 생성
        if (comment.getParentId() == null) {
            // 게시글에 대한 댓글
            Post post = postMapper.getPostById(comment.getPostId());
            notificationService.createCommentNotification(comment, comment.getPostId(), post.getUserId()); // .longValue() 제거
        } else {
            // 댓글에 대한 답글
            Comment parentComment = commentMapper.getCommentById(comment.getParentId());
            notificationService.createReplyNotification(comment, comment.getParentId(), parentComment.getUserId());
        }

        return comment.getCommentId();
    }

    public void updateComment(Comment comment) {
        commentMapper.updateComment(comment);
    }

    public void deleteComment(Integer commentId) {
        commentMapper.deleteComment(commentId);
    }

    public void toggleCommentLike(Integer commentId, Long userId, boolean isLike) {
        CommentLike existingLike = commentMapper.getCommentLike(commentId, userId);

        if (existingLike == null) {
            // 좋아요/싫어요가 없는 경우 추가
            CommentLike commentLike = new CommentLike();
            commentLike.setCommentId(commentId);
            commentLike.setUserId(userId);
            commentLike.setLike(isLike);

            commentMapper.insertCommentLike(commentLike);

            // 알림 생성 (좋아요인 경우만)
            if (isLike) {
                Comment comment = commentMapper.getCommentById(commentId);
                notificationService.createLikeNotification(commentLike, comment.getUserId());
            }
        } else if (existingLike.isLike() != isLike) {
            // 좋아요/싫어요 상태가 다른 경우 업데이트
            existingLike.setLike(isLike);
            commentMapper.updateCommentLike(existingLike);
        } else {
            // 같은 상태인 경우 삭제 (토글)
            commentMapper.deleteCommentLike(commentId, userId);
        }
    }
}