package kr.or.kosa.snippets.community.mapper;

import kr.or.kosa.snippets.community.model.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {
    /**
     * 사용자별 알림 목록 조회 (최신순)
     */
    List<Notification> getNotificationsByUserId(@Param("userId") Long userId);

    /**
     * 안읽은 알림 수 조회
     */
    int getUnreadNotificationCount(@Param("userId") Long userId);

    /**
     * 알림 생성
     */
    void insertNotification(Notification notification);

    /**
     * 알림 읽음 처리
     */
    void markAsRead(@Param("notificationId") Integer notificationId);

    /**
     * 모든 알림 읽음 처리
     */
    void markAllAsRead(@Param("userId") Long userId);

    /**
     * 알림 삭제
     */
    void deleteNotification(@Param("notificationId") Integer notificationId);

    /**
     * 특정 알림 조회
     */
    Notification getNotificationById(@Param("notificationId") Integer notificationId);
}