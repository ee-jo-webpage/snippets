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
/**
 * 로그인 실패 횟수 및 패턴을 기반으로 IP 차단 로직을 수행하는 서비스
 * - 일정 시간 내 과도한 시도
 * - 동일 User-Agent 반복 시도
 * - 차단 이력 DB에 저장
 */

@Component
@RequiredArgsConstructor
public class LoginAttemptService {

    private final BlockedIpLogMapper blockedIpLogMapper;
    private final IpLocationService ipLocationService;
    // 기본 로그인 실패 허용 횟수
    private static final int MAX_ATTEMPTS = 5;
    // 차단 기간
    private static final Duration BLOCK_DURATION = Duration.ofDays(5);
    // 시도 간격
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
    /**
     * 해당 IP의 모든 실패 기록 초기화
     * - IP 실패 횟수
     * - 빠른 시도 횟수
     * - UA 반복 횟수
     */

    public void reset(String ip) {
        attempts.remove(ip);
        ipRequestTimestamps.remove(ip);
        uaAttempts.keySet().removeIf(k -> k.startsWith(ip + "::"));
    }
    /**
     * 해당 IP가 현재 차단 상태인지 확인
     * - 차단 시간이 남아 있으면 true
     * - 차단 시간이 지나면 기록 제거
     */

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
    /**
     * 로그인 실패 시 호출
     * - IP별 실패 횟수 누적
     * - 빠른 시도 감지 (5초 이내 연속 요청)
     * - 동일 User-Agent 반복 요청 감지
     * - 조건 충족 시 차단 + DB 기록
     */

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
    /**
     * 서버 시작 시 DB 차단 목록을 메모리에 복원할 때 사용
     */
    public void restoreBlockedIp(String ip, LocalDateTime unblockAt) {
        attempts.put(ip, new LoginAttemptInfo(MAX_ATTEMPTS, unblockAt));
    }
    /**
     * User-Agent 문자열로 봇 또는 자동화 도구 여부 판별
     */

    private boolean isBotUserAgent(String ua) {
        if (ua == null) return false;
        return ua.toLowerCase().matches(".*(bot|spider|crawl|httpclient|wget|curl|python).*");
    }
}