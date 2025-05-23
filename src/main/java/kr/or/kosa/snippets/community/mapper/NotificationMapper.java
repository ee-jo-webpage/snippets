package kr.or.kosa.snippets.community.mapper;

import kr.or.kosa.snippets.community.model.Notification;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NotificationMapper {
    List<Notification> getNotificationByUserId(Long userId);
    void insertNotification(Notification notification);
    void markAsRead(Long notificationId);
    void markAllAsRead(Long userId);
    void deleteNotification(Integer notificationId);
}
