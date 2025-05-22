package kr.or.kosa.snippets.user.service;

import jakarta.validation.Valid;
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
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public String getNicknameByEmail(String email) {
        Users user = userMapper.findByEmail(email);
        return user != null ? user.getNickname() : "알 수 없음";
    }

    public Users findByEmail(String email) {
        Users user = userMapper.findByEmail(email);
        if (user == null) throw new UsernameNotFoundException("사용자 없음");
        return user;
    }

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


    public void revokeGoogleAccessToken(String accessToken) {
        String revokeUrl = "https://oauth2.googleapis.com/revoke?token=" + accessToken;

        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForLocation(revokeUrl, null);
            log.info("✅ 구글 accessToken 해제 성공: {}", accessToken);
        } catch (Exception e) {
            log.warn("⚠️ 구글 accessToken 해제 실패: {}", e.getMessage());
        }
    }
}