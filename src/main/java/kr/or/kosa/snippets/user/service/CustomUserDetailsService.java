package kr.or.kosa.snippets.user.service;

import kr.or.kosa.snippets.user.mapper.UserMapper;
import kr.or.kosa.snippets.user.model.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Users user = userMapper.findByEmail(username);
        if(user == null) {
            throw new UsernameNotFoundException("이메일 없음: " + username);
        }
        log.info("사용자 조회 시도: {}", username);
        log.info("패스워드(DB): {}", user.getPassword());
        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRole())
                .disabled(!user.isEnabled())  // 이메일 인증 안 된 사용자 로그인 막기
                .build();
    }

}