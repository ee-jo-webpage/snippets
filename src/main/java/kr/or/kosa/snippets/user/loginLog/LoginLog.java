package kr.or.kosa.snippets.user.loginLog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginLog {
    private Long id;
    private String email;
    private String ip;
    private String userAgent;
    private LocalDateTime loginAt;
    private boolean success;
}