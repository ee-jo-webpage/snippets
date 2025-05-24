package kr.or.kosa.snippets.user.blockIp;

import jakarta.annotation.PostConstruct;
import kr.or.kosa.snippets.user.loginLog.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BlockedIpLogLoader {

    private final BlockedIpLogMapper blockedIpLogMapper;
    private final LoginAttemptService loginAttemptService;

    @PostConstruct
    public void preloadBlockedIps() {
        List<BlockedIpLog> blockedIps = blockedIpLogMapper.findAllStillBlocked(LocalDateTime.now());
        for (BlockedIpLog log : blockedIps) {
            loginAttemptService.restoreBlockedIp(log.getIp(), log.getUnblockAt());
        }
    }
}