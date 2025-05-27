package kr.or.kosa.snippets.community.service;

import kr.or.kosa.snippets.community.mapper.CommentMapper;
import kr.or.kosa.snippets.community.mapper.PostMapper;
import kr.or.kosa.snippets.community.model.Comment;
import kr.or.kosa.snippets.community.model.CommentLike;
import kr.or.kosa.snippets.community.model.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {
    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final NotificationService notificationService;

    public List<Comment> getCommentsByPostId(Integer postId, Long currentUserId) {
        // 모든 댓글을 한 번에 가져오기
        List<Comment> allComments = commentMapper.getCommentsByPostId(postId);

        // 계층 구조로 변환
        return buildCommentTree(allComments, currentUserId);
    }

    /**
     * 댓글 리스트를 트리 구조로 변환
     */
    private List<Comment> buildCommentTree(List<Comment> allComments, Long currentUserId) {
        // parentId별로 그룹화
        Map<Integer, List<Comment>> commentsByParent = new HashMap<>();
        List<Comment> rootComments = new ArrayList<>();

        for (Comment comment : allComments) {
            // 좋아요 상태 설정
            setLikeStatus(comment, currentUserId);

            if (comment.getParentId() == null) {
                rootComments.add(comment);
            } else {
                commentsByParent.computeIfAbsent(comment.getParentId(), k -> new ArrayList<>())
                        .add(comment);
            }
        }

        // 각 댓글에 대해 재귀적으로 자식 댓글 설정
        for (Comment rootComment : rootComments) {
            setChildComments(rootComment, commentsByParent);
        }

        return rootComments;
    }

    /**
     * 재귀적으로 자식 댓글 설정
     */
    private void setChildComments(Comment parent, Map<Integer, List<Comment>> commentsByParent) {
        List<Comment> children = commentsByParent.get(parent.getCommentId());
        if (children != null) {
            parent.setChildComments(children);
            // 각 자식에 대해서도 재귀적으로 처리
            for (Comment child : children) {
                setChildComments(child, commentsByParent);
            }
        }
    }

    /**
     * 좋아요 상태 설정
     */
    private void setLikeStatus(Comment comment, Long currentUserId) {
        if (currentUserId != null) {
            CommentLike like = commentMapper.getCommentLike(comment.getCommentId(), currentUserId);
            if (like != null) {
                comment.setIsLiked(like.isLike());
                comment.setIsDisliked(!like.isLike());
            } else {
                comment.setIsLiked(false);
                comment.setIsDisliked(false);
            }
        }
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
            notificationService.createCommentNotification(comment, comment.getPostId(), post.getUserId());
        } else {
            // 댓글에 대한 답글 (모든 레벨의 대댓글 포함)
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