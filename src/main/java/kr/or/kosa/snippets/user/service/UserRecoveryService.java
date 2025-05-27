package kr.or.kosa.snippets.user.service;

import kr.or.kosa.snippets.user.mapper.UserMapper;
import kr.or.kosa.snippets.user.model.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRecoveryService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    /**
     * 계정 비활성화
     */
    @Transactional
    public void deactivateUser(String email, String reason) {
        Users user = userMapper.findByEmail(email);
        if (user == null) throw new IllegalArgumentException("사용자 없음");

        user.setEnabled(false);
        user.setDeletedAt(LocalDateTime.now());
        user.setReason(reason);
        userMapper.updateUser(user);
    }

    @Transactional
    public void restoreAccount(String email) {
        int updated = userMapper.restoreUser(email);
        if (updated == 0) {
            throw new IllegalArgumentException("복구 가능한 계정을 찾을 수 없습니다.");
        }
    }

    /**
     * 비활성화된 사용자 계정 복구
     * - DB에서 deleted_at을 null로 바꾸고 enabled를 true로 변경
     * - 사용자가 복구 가능한 상태가 아니면 예외 발생
     */
    @Transactional
    public void recoverAccount(String email) {
        Users user = userMapper.findByEmail(email);

        // 복구 불가능한 상황: 존재하지 않거나, 이미 활성화 상태이거나, deletedAt이 null인 경우
        if (user == null || user.isEnabled() || user.getDeletedAt() == null) {
            throw new IllegalArgumentException("복구할 수 없는 계정입니다.");
        }

        System.out.println("복구 대상 확인: " +
                "user=" + user +
                ", enabled=" + user.isEnabled() +
                ", deletedAt=" + user.getDeletedAt());

        user.setEnabled(true);
        user.setDeletedAt(null);
        user.setReason(null);
        userMapper.updateUser(user);
        log.info("계정 복구 완료 - {}", email);
    }

    // 비활성화 계정 찾기
    /**
     * DB에서 deletedAt != null인 유저 중 email 일치하는 계정 찾기
     * - 주로 복구 가능한 계정인지 확인할 때 사용
     */
    public Users findDeletedUserByEmail(String email) {
        return userMapper.findDeletedUser(email);
    }
    /**
     * 복구 시도
     * - userMapper.restoreUser는 DB에서 enable을 true로, deletedAt을 null로 바꾸는 쿼리
     * - 복구 성공 여부를 boolean으로 반환
     */
    public boolean restoreUser(String email) {
        return userMapper.restoreUser(email) > 0;
    }

    /**
     * 임시 비밀번호를 생성하여 이메일로 전송
     * - LOCAL 로그인 사용자만 가능
     * - 기존 비밀번호는 임시값으로 덮어씀
     */
    @Transactional
    public void sendTemporaryPassword(String email) {
        Users user = userMapper.findByEmail(email);
//        if (user == null) throw new IllegalArgumentException("유저 없음");
        if (user == null || !"LOCAL".equals(user.getLoginType())) {
            throw new IllegalStateException("소셜 계정은 비밀번호 변경이 불가능합니다.");
        }
        String tempPassword = createRandomPassword();
        user.setPassword(passwordEncoder.encode(tempPassword));
        userMapper.updateUser(user);

        mailService.sendTemporaryPassword(user.getEmail(), tempPassword);
        log.info("임시 비밀번호 전송: email={}, password={}", user.getEmail(), tempPassword);
    }

    private String createRandomPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }


}