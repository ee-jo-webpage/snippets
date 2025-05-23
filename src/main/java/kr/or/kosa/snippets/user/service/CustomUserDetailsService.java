package kr.or.kosa.snippets.user.service;

import kr.or.kosa.snippets.user.mapper.UserMapper;
import kr.or.kosa.snippets.user.model.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Users user = userMapper.findByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("이메일 없음: " + username);
        }

        log.info("사용자 조회 시도: {}", username);

        return new CustomUserDetails(
                user.getUserId(),
                user.getEmail(),
                user.getPassword(),
                user.getNickname(),
                user.isEnabled(),
                user.getLoginType(),
                List.of(new SimpleGrantedAuthority(user.getRole())),
                Map.of() // attributes 비워서 넘김 (일반 로그인엔 필요 없음)
        );
    }
}