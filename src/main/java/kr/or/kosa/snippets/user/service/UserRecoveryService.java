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
     * 비활성화 계정 활성화
     */
    @Transactional
    public void recoverAccount(String email) {
        Users user = userMapper.findByEmail(email);
        // 이메일이 null 이거나 isEnabled 가 1이거나 ,
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
    public Users findDeletedUserByEmail(String email) {
        return userMapper.findDeletedUser(email);
    }

    public boolean restoreUser(String email) {
        return userMapper.restoreUser(email) > 0;
    }


}