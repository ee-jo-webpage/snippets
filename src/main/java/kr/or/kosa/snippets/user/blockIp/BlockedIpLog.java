package kr.or.kosa.snippets.user.blockIp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockedIpLog {
    private Long id;
    private String ip;
    private String country;
    private String city;
    private String userAgent;
    private boolean isBot;
    private LocalDateTime blockedAt;
    private LocalDateTime unblockAt;
}