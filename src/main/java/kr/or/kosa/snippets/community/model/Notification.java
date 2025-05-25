package kr.or.kosa.snippets.community.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Notification {
    private Integer notificationId;
    private Long userId;
    private Long senderId;
    private String type; // new_comment, new_reply, comment_like 등
    private Integer targetId;
    private boolean isRead;
    private LocalDateTime createdAt;

    // 조회용 추가 필드
    private String senderNickname;
    private String message;
}