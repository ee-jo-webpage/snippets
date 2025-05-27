package kr.or.kosa.snippets.user.service;

import kr.or.kosa.snippets.user.mapper.UserMapper;
import kr.or.kosa.snippets.user.model.UserDTO;
import kr.or.kosa.snippets.user.model.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
/**
 * 사용자 인증 관련 서비스
 * - 회원가입
 * - 이메일 인증 코드 전송 및 확인
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    /**
     * 회원가입 처리
     * - 이메일 중복 / 탈퇴 / 미인증 상태 확인
     * - 비밀번호 확인 일치 여부 검사
     * - DB에 유저 등록 (enabled=false)
     * - 이메일 인증 코드 생성 및 발송
     */
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
                .loginType("LOCAL")
                .createdAt(LocalDateTime.now())
                .role("ROLE_USER")
                .build();

        userMapper.insertUser(user);

        String code = CodeGenerator.generateCode();
        mailService.saveVerificationCode(user.getEmail(), code, 10);
        mailService.sendVerificationCode(user.getEmail(), code);
    }
    /**
     * 이메일 인증 코드 재전송
     * - 이미 인증된 경우 예외
     */
    @Transactional
    public void resendVerificationCode(String email) {
        Users user = userMapper.findByEmail(email);
        if (user == null) throw new IllegalArgumentException("사용자 없음");
        if (user.isEnabled()) throw new IllegalStateException("이미 인증된 사용자입니다.");

        String code = CodeGenerator.generateCode();
        mailService.saveVerificationCode(email, code, 10);
        mailService.sendVerificationCode(email, code);
    }

    /**
     * 이메일 인증 코드 확인
     * - 입력된 코드와 저장된 코드 비교
     * - 성공 시 사용자 활성화 처리
     */
    @Transactional
    public void verifyEmailCode(String email, String inputCode) {
        if (!mailService.verifyCode(email, inputCode)) {
            throw new IllegalArgumentException("인증 코드가 일치하지 않거나 만료되었습니다.");
        }

        Users user = userMapper.findByEmail(email);
        if (user == null) throw new IllegalArgumentException("사용자 없음");

        user.setEnabled(true);
        userMapper.updateUser(user);
        mailService.deleteCode(email);
    }
}