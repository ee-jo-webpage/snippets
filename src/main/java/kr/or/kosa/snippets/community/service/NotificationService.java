package kr.or.kosa.snippets.community.service;

import kr.or.kosa.snippets.community.mapper.NotificationMapper;
import kr.or.kosa.snippets.community.model.Comment;
import kr.or.kosa.snippets.community.model.CommentLike;
import kr.or.kosa.snippets.community.model.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationMapper notificationMapper;

    /**
     * 알림 생성 (기본 메서드)
     */
    public void createNotification(Long userId, Long senderId, String type, Integer targetId) {
        // 본인에게는 알림을 보내지 않음
        if (userId.equals(senderId)) {
            return;
        }

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setSenderId(senderId);
        notification.setType(type);
        notification.setTargetId(targetId);
        notification.setRead(false);

        notificationMapper.insertNotification(notification);
    }

    /**
     * 댓글 알림 생성
     */
    public void createCommentNotification(Comment comment, Integer postId, Long postAuthorId) {
        // 게시글 작성자에게 댓글 알림
        createNotification(postAuthorId, comment.getUserId(), "new_comment", postId);
    }

    /**
     * 답글 알림 생성
     */
    public void createReplyNotification(Comment reply, Integer parentCommentId, Long parentCommentAuthorId) {
        // 원 댓글 작성자에게 답글 알림
        createNotification(parentCommentAuthorId, reply.getUserId(), "new_reply", parentCommentId);
    }

    /**
     * 댓글 좋아요 알림 생성
     */
    public void createLikeNotification(CommentLike like, Long commentAuthorId) {
        // 댓글 작성자에게 좋아요 알림 (좋아요인 경우만)
        if (like.isLike()) {
            createNotification(commentAuthorId, like.getUserId(), "comment_like", like.getCommentId());
        }
    }

    /**
     * 사용자별 알림 목록 조회
     */
    public List<Notification> getNotificationsByUserId(Long userId) {
        return notificationMapper.getNotificationsByUserId(userId);
    }

    /**
     * 안읽은 알림 수 조회
     */
    public int getUnreadNotificationCount(Long userId) {
        return notificationMapper.getUnreadNotificationCount(userId);
    }

    /**
     * 알림 읽음 처리
     */
    public void markAsRead(Integer notificationId) {
        notificationMapper.markAsRead(notificationId);
    }

    /**
     * 모든 알림 읽음 처리
     */
    public void markAllAsRead(Long userId) {
        notificationMapper.markAllAsRead(userId);
    }

    /**
     * 알림 삭제
     */
    public void deleteNotification(Integer notificationId) {
        notificationMapper.deleteNotification(notificationId);
    }
}