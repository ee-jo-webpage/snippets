package kr.or.kosa.snippets.user.loginLog;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginLogService {
    private final LoginLogMapper loginLogMapper;
    private final Queue<LoginLog> logBuffer = new ConcurrentLinkedQueue<>();

    public void logLogin(String email, HttpServletRequest request, boolean success) {
        LoginLog log = LoginLog.builder()
                .email(email)
                .ip(request.getRemoteAddr())
                .userAgent(request.getHeader("User-Agent"))
                .loginAt(LocalDateTime.now())
                .success(success)
                .build();

        logBuffer.add(log);
    }

    @Scheduled(fixedRate = 1 * 60 * 1000) // 5분마다
    public void flushLogsToDb() {
        List<LoginLog> logs = new ArrayList<>();
        while (!logBuffer.isEmpty()) {
            logs.add(logBuffer.poll());
        }

        if (!logs.isEmpty()) {
            loginLogMapper.insertLoginLogs(logs);
            log.info("로그 {}건 DB 저장됨", logs.size());
        }
    }

    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시
    public void deleteOldLogs() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        loginLogMapper.deleteOldLogs(cutoff);
        log.info("1주일 지난 로그인 로그 삭제 (기준: {})", cutoff);
    }
}