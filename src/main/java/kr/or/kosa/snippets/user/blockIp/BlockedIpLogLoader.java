package kr.or.kosa.snippets.user.blockIp;

import jakarta.annotation.PostConstruct;
import kr.or.kosa.snippets.user.loginLog.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
/**
 * BlockedIpLog 테이블에 저장된 차단 이력 중
 * 아직 차단이 해제되지 않은 IP들을
 * 애플리케이션 메모리(LoginAttemptService)로 복구하는 컴포넌트
 * 서버가 재시작되더라도 이전 차단 상태를 유지할 수 있게 해줌
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class BlockedIpLogLoader {

    private final BlockedIpLogMapper blockedIpLogMapper;
    private final LoginAttemptService loginAttemptService;

    @PostConstruct
    public void preloadBlockedIps() {
        // DB에서 현재 차단 중인 IP 목록을 조회 (unblockAt > NOW())
        List<BlockedIpLog> blockedIps = blockedIpLogMapper.findAllStillBlocked(LocalDateTime.now());
        // 각 차단 IP 정보를 메모리에 로딩
        for (BlockedIpLog log : blockedIps) {
            loginAttemptService.restoreBlockedIp(log.getIp(), log.getUnblockAt());
        }
    }
}