package kr.or.kosa.snippets.user.service;

import kr.or.kosa.snippets.user.mapper.UserMapper;
import kr.or.kosa.snippets.user.model.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRecoveryService {
    private final UserMapper userMapper;


    @Transactional
    public void deactivateUser(String email, String reason) {
        Users user = userMapper.findByEmail(email);
        if (user == null) throw new IllegalArgumentException("사용자 없음");

        user.setEnabled(false);
        user.setDeletedAt(LocalDateTime.now());
        user.setReason(reason);
        userMapper.updateUser(user);
    }


}