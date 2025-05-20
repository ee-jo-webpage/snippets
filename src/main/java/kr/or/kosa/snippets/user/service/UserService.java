package kr.or.kosa.snippets.user.service;

import jakarta.validation.Valid;
import kr.or.kosa.snippets.user.mapper.UserMapper;
import kr.or.kosa.snippets.user.model.UserUpdateDTO;
import kr.or.kosa.snippets.user.model.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
}