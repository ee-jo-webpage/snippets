package kr.or.kosa.snippets.community.controller;

import kr.or.kosa.snippets.community.model.Notification;
import kr.or.kosa.snippets.community.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/board/notification")
@RequiredArgsConstructor
public class NotificationRestController {
    private final NotificationService notificationService;

    /**
     * 알림 목록 조회
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        // Long userId = ((CustomUserDetails) userDetails).getUserId();
        Long userId = 1L; // 임시

        List<Notification> notifications = notificationService.getNotificationsByUserId(userId);
        int unreadCount = notificationService.getUnreadNotificationCount(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("notifications", notifications);
        response.put("unreadCount", unreadCount);

        return ResponseEntity.ok(response);
    }

    /**
     * 안읽은 알림 수 조회
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        // Long userId = ((CustomUserDetails) userDetails).getUserId();
        Long userId = 1L; // 임시

        int unreadCount = notificationService.getUnreadNotificationCount(userId);

        return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
    }

    /**
     * 알림 읽음 처리
     */
    @PostMapping("/read/{notificationId}")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Integer notificationId) {
        notificationService.markAsRead(notificationId);

        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 모든 알림 읽음 처리
     */
    @PostMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(@AuthenticationPrincipal UserDetails userDetails) {
        // Long userId = ((CustomUserDetails) userDetails).getUserId();
        Long userId = 1L; // 임시

        notificationService.markAllAsRead(userId);

        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 알림 삭제
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable Integer notificationId) {
        notificationService.deleteNotification(notificationId);

        return ResponseEntity.ok(Map.of("success", true));
    }
}