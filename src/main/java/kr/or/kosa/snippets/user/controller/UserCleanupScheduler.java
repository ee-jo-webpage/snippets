package kr.or.kosa.snippets.user.controller;

import kr.or.kosa.snippets.user.mapper.UserMapper;
import kr.or.kosa.snippets.user.model.Users;
import kr.or.kosa.snippets.user.service.CustomOAuth2UserService;
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
    private final CustomOAuth2UserService customOAuth2UserService;

    /**
     * 비활성 + 삭제 시간 지난 계정 정리
     * - enabled = false AND deletedAt < 기준 시간인 사용자 삭제
     * - 소셜 로그인 계정일 경우 Google 연결 해제 시도
     * <p>
     * [실행 주기]
     * - 매 5분마다 실행
     */
    @Transactional
    @Scheduled(cron = "0 */5 * * * *") // 매 5분마다 0초에 실행
    public void deleteExpiredUsers() {
        log.info(" [비활성 계정 삭제] 실행");

        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(1); // 예시용: 1분 전
        List<Users> expiredUsers = userMapper.findAllByEnabledFalseAndDeletedAtBefore(cutoff);

        for (Users user : expiredUsers) {
            if ("GOOGLE".equalsIgnoreCase(user.getLoginType())) {
                log.info(" [구글 계정 revoke] 시도: {}", user.getEmail());

                // accessToken은 없음 → revoke 실패 감수
                try {
                    customOAuth2UserService.deleteUser(user.getEmail(), null); // accessToken 없이 호출
                } catch (Exception e) {
                    log.warn(" 구글 revoke 실패 (무시하고 삭제 진행): {}", e.getMessage());
                }
            }
        }

        if (!expiredUsers.isEmpty()) {
            userMapper.deleteAll(expiredUsers);
            log.info(" {}명의 비활성 사용자 삭제 완료", expiredUsers.size());
        } else {
            log.info("삭제할 비활성 사용자가 없습니다.");
        }
    }

    /**
     * 미인증 + 생성된 지 1분 이상 지난 계정 정리
     * - enabled = false AND createdAt < 기준 시간
     * <p>
     * [실행 주기]
     * - 매 5분마다 30초에 실행
     */
    @Scheduled(cron = "30 */5 * * * *") // 매 5분마다 30초에 실행
    public void deleteInactiveUnverifiedUsers() {
        log.info(" [미인증 계정 정리] 시작");

        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(1); // 1분 전 기준
        List<Users> users = userMapper.existsByEmailAndEnabledFalseBefore(cutoff);
        if (!users.isEmpty()) {
            userMapper.deleteAll(users);
            log.info(" [미인증 계정 정리] {}명 삭제 완료", users.size());
        } else {
            log.info(" [미인증 계정 정리] 삭제할 사용자 없음");
        }
    }
}