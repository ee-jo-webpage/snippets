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
/**
 * 로그인 시도 기록을 메모리에 임시 저장한 뒤
 * 일정 주기로 DB에 저장하고,
 * 오래된 로그는 주기적으로 삭제하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoginLogService {
    private final LoginLogMapper loginLogMapper;
    // 로그인 로그를 임시 저장하는 비동기 큐
    private final Queue<LoginLog> logBuffer = new ConcurrentLinkedQueue<>();
    /**
     * 로그인 시도 시 호출
     * - email, IP, User-Agent, 성공 여부를 기록
     * - DB에 바로 저장하지 않고 메모리에 임시 저장
     */
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

    @Scheduled(fixedRate = 5 * 60 * 1000) // 5분마다
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