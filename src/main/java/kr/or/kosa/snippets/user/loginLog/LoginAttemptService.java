package kr.or.kosa.snippets.user.loginLog;

import jakarta.annotation.PostConstruct;
import kr.or.kosa.snippets.user.blockIp.BlockedIpLog;
import kr.or.kosa.snippets.user.blockIp.BlockedIpLogMapper;
import kr.or.kosa.snippets.user.blockIp.IpLocationService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class LoginAttemptService {

    private final BlockedIpLogMapper blockedIpLogMapper;
    private final IpLocationService ipLocationService;

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration BLOCK_DURATION = Duration.ofDays(5);

    private static final Duration ATTEMPT_INTERVAL = Duration.ofSeconds(5);
    private static final int MAX_SAME_UA_COUNT = 10;

    // IP별 시도 기록
    private final Map<String, LoginAttemptInfo> attempts = new ConcurrentHashMap<>();

    // IP별 최근 시도 간격 체크용
    private final Map<String, AttemptMetadata> ipRequestTimestamps = new ConcurrentHashMap<>();

    // IP + UserAgent 조합 추적
    private final Map<String, Integer> uaAttempts = new ConcurrentHashMap<>();

    @Getter
    @AllArgsConstructor
    private static class LoginAttemptInfo {
        private final int count;
        private final LocalDateTime blockedUntil;
    }

    @Getter
    @AllArgsConstructor
    private static class AttemptMetadata {
        private final int count;
        private final LocalDateTime lastAttempt;
    }

    public void reset(String ip) {
        attempts.remove(ip);
        ipRequestTimestamps.remove(ip);
        uaAttempts.keySet().removeIf(k -> k.startsWith(ip + "::"));
    }

    public boolean isBlocked(String ip) {
        LoginAttemptInfo info = attempts.get(ip);
        if (info == null) return false;

        if (info.blockedUntil != null && info.blockedUntil.isAfter(LocalDateTime.now())) {
            return true;
        }

        if (info.blockedUntil != null && info.blockedUntil.isBefore(LocalDateTime.now())) {
            attempts.remove(ip);
        }

        return false;
    }

    public void recordFailure(String ip, String userAgent) {
        // 일반 실패 횟수 증가
        LoginAttemptInfo info = attempts.getOrDefault(ip, new LoginAttemptInfo(0, null));
        int newCount = info.count + 1;
        LocalDateTime blockedUntil = null;

        // 1. 시도 간격 감지
        AttemptMetadata meta = ipRequestTimestamps.getOrDefault(ip, new AttemptMetadata(0, LocalDateTime.MIN));
        Duration interval = Duration.between(meta.getLastAttempt(), LocalDateTime.now());
        int fastAttemptCount = (interval.compareTo(ATTEMPT_INTERVAL) <= 0) ? meta.getCount() + 1 : 1;
        ipRequestTimestamps.put(ip, new AttemptMetadata(fastAttemptCount, LocalDateTime.now()));

        // 2. (IP + UA) 조합 시도 감지
        String key = ip + "::" + userAgent;
        int uaCount = uaAttempts.getOrDefault(key, 0) + 1;
        uaAttempts.put(key, uaCount);

        // 차단 조건 충족 시
        boolean overBasicFail = newCount >= MAX_ATTEMPTS;
        boolean abnormalByTime = fastAttemptCount >= MAX_ATTEMPTS;
        boolean abnormalByUa = uaCount >= MAX_SAME_UA_COUNT;

        if (overBasicFail || abnormalByTime || abnormalByUa) {
            blockedUntil = LocalDateTime.now().plus(BLOCK_DURATION);

            boolean isBot = isBotUserAgent(userAgent);
            String country = ipLocationService.getCountry(ip);
            String city = ipLocationService.getCity(ip);

            BlockedIpLog log = BlockedIpLog.builder()
                    .ip(ip)
                    .userAgent(userAgent)
                    .isBot(isBot)
                    .blockedAt(LocalDateTime.now())
                    .unblockAt(blockedUntil)
                    .country(country)
                    .city(city)
                    .build();

            blockedIpLogMapper.insertBlockedIpLog(log);
        }

        // 갱신
        attempts.put(ip, new LoginAttemptInfo(newCount, blockedUntil));
    }

    public void restoreBlockedIp(String ip, LocalDateTime unblockAt) {
        attempts.put(ip, new LoginAttemptInfo(MAX_ATTEMPTS, unblockAt));
    }

    private boolean isBotUserAgent(String ua) {
        if (ua == null) return false;
        return ua.toLowerCase().matches(".*(bot|spider|crawl|httpclient|wget|curl|python).*");
    }
}