package kr.or.kosa.snippets.user.service;

import kr.or.kosa.snippets.user.mapper.UserMapper;
import kr.or.kosa.snippets.user.model.UserDTO;
import kr.or.kosa.snippets.user.model.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    @Transactional
    public void register(UserDTO dto) {
        if (userMapper.existsByEmailAndDisabledAndDeleted(dto.getEmail())) {
            throw new IllegalArgumentException("해당 이메일은 이미 탈퇴 처리된 계정입니다.");
        }
        if (userMapper.existsByEmailAndEnabledFalse(dto.getEmail())) {
            throw new IllegalArgumentException("이메일 인증을 완료하지 않은 계정이 존재합니다.");
        }
        if (userMapper.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }


        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        Users user = Users.builder()
                .email(dto.getEmail())
                .nickname(dto.getNickname())
                .password(passwordEncoder.encode(dto.getPassword()))
                .enabled(false)
                .createdAt(LocalDateTime.now())
                .role("ROLE_USER")
                .build();

        userMapper.insertUser(user);
//
//        String code = CodeGenerator.generateCode();
//        mailService.saveVerificationCode(user.getEmail(), code, 10);
//        mailService.sendVerificationCode(user.getEmail(), code);
    }

}