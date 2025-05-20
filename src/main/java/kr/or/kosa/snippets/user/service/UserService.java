package kr.or.kosa.snippets.user.service;

import kr.or.kosa.snippets.user.mapper.UserMapper;
import kr.or.kosa.snippets.user.model.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    public String getNicknameByEmail(String email) {
        Users user = userMapper.findByEmail(email);
        return user != null ? user.getNickname() : "알 수 없음";
    }

}