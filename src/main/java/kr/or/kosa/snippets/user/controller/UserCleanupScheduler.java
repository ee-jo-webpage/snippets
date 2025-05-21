package kr.or.kosa.snippets.user.controller;

import kr.or.kosa.snippets.user.mapper.UserMapper;
import kr.or.kosa.snippets.user.model.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
@Component
@RequiredArgsConstructor
@Slf4j
public class UserCleanupScheduler {
    private final UserMapper userMapper;

    @Transactional
    @Scheduled(cron = "0 * * * * *") // 매 분 0초마다 실행
    public void deleteExpiredUsers() {
        log.info("🔧 [비활성 계정 삭제] 실행");

        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(1); // 1분 전 기준
        List<Users> expiredUsers = userMapper.findAllByEnabledFalseAndDeletedAtBefore(cutoff);
        if (!expiredUsers.isEmpty()) {
            userMapper.deleteAll(expiredUsers);
            log.info("✅ {}명의 비활성 사용자 삭제 완료", expiredUsers.size());
        } else {
            log.info("ℹ️ 삭제할 비활성 사용자가 없습니다.");
        }
    }

    @Scheduled(cron = "30 * * * * *") // 매 분 30초마다 실행
    public void deleteInactiveUnverifiedUsers(){
        log.info("🔧 [미인증 계정 정리] 시작");

        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(1); // 1분 전 기준
        List<Users> users = userMapper.existsByEmailAndEnabledFalseBefore(cutoff);
        if(!users.isEmpty()){
            userMapper.deleteAll(users);
            log.info("✅ [미인증 계정 정리] {}명 삭제 완료", users.size());
        } else {
            log.info("ℹ️ [미인증 계정 정리] 삭제할 사용자 없음");
        }
    }
}