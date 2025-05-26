package kr.or.kosa.snippets.community.controller;

import kr.or.kosa.snippets.community.model.Notification;
import kr.or.kosa.snippets.community.service.NotificationService;
import kr.or.kosa.snippets.user.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/community/notification")
@RequiredArgsConstructor
public class NotificationRestController {
    private final NotificationService notificationService;

    /**
     * 알림 목록 조회
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getNotifications(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // 로그인 체크
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            Long userId = userDetails.getUserId();
            List<Notification> notifications = notificationService.getNotificationsByUserId(userId);
            int unreadCount = notificationService.getUnreadNotificationCount(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("notifications", notifications);
            response.put("unreadCount", unreadCount);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("알림 목록 조회 오류: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "알림을 불러올 수 없습니다."));
        }
    }

    /**
     * 안읽은 알림 수 조회
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // 로그인 체크
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            Long userId = userDetails.getUserId();
            int unreadCount = notificationService.getUnreadNotificationCount(userId);

            return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
        } catch (Exception e) {
            System.out.println("안읽은 알림 수 조회 오류: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "안읽은 알림 수를 불러올 수 없습니다."));
        }
    }

    /**
     * 알림 읽음 처리
     */
    @PostMapping("/read/{notificationId}")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Integer notificationId) {
        try {
            notificationService.markAsRead(notificationId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            System.out.println("알림 읽음 처리 오류: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "알림 읽음 처리에 실패했습니다."));
        }
    }

    /**
     * 모든 알림 읽음 처리
     */
    @PostMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // 로그인 체크
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            Long userId = userDetails.getUserId();
            notificationService.markAllAsRead(userId);

            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            System.out.println("모든 알림 읽음 처리 오류: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "모든 알림 읽음 처리에 실패했습니다."));
        }
    }

    /**
     * 알림 삭제
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable Integer notificationId) {
        try {
            notificationService.deleteNotification(notificationId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            System.out.println("알림 삭제 오류: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "알림 삭제에 실패했습니다."));
        }
    }
}