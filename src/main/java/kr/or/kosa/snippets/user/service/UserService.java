package kr.or.kosa.snippets.user.service;

import kr.or.kosa.snippets.user.mapper.UserMapper;
import kr.or.kosa.snippets.user.model.UserUpdateDTO;
import kr.or.kosa.snippets.user.model.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

/**
 * 사용자 정보 조회 및 수정, 비밀번호 변경,
 * 소셜 로그인 accessToken 해제 등 사용자 계정 관련 기능 처리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    /**
     * 이메일을 기반으로 닉네임 조회
     * - 사용자 없을 경우 '알 수 없음' 반환
     */
    public String getNicknameByEmail(String email) {
        Users user = userMapper.findByEmail(email);
        return user != null ? user.getNickname() : "알 수 없음";
    }
    /**
     * 이메일로 사용자 조회
     * - 없을 경우 예외 발생
     */
    public Users findByEmail(String email) {
        Users user = userMapper.findByEmail(email);
        if (user == null) throw new UsernameNotFoundException("사용자 없음");
        return user;
    }
    /**
     * 이메일로 사용자 조회
     * - 없을 경우 예외 발생
     */
    @Transactional
    public void updateUserInfo(String email, UserUpdateDTO dto) {
        Users user = userMapper.findByEmail(email);
        if (user == null) throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");

        user.setNickname(dto.getNickname());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateUser(user);
    }

    /**
     * 비밀번호 변경 처리
     * - 현재 비밀번호 일치 여부 확인 후 변경
     */
    public void changePassword(String email, String current, String newPw) {
        Users user = userMapper.findByEmail(email);
        if (user == null || !passwordEncoder.matches(current, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        String encoded = passwordEncoder.encode(newPw);
        user.setPassword(encoded);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateUser(user);
    }

    /**
     * 구글 accessToken 해제 요청
     * - 탈퇴 시 Google 계정과의 연결을 끊기 위한 처리
     */
    public void revokeGoogleAccessToken(String accessToken) {
        String revokeUrl = "https://oauth2.googleapis.com/revoke?token=" + accessToken;

        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForLocation(revokeUrl, null);
        } catch (Exception e) {
            log.warn("️ 구글 accessToken 해제 실패: {}", e.getMessage());
        }
    }
}