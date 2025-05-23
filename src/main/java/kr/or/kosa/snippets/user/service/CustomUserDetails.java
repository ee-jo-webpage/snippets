package kr.or.kosa.snippets.user.service;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Getter
public class CustomUserDetails implements UserDetails, OAuth2User {

    private final Long userId;
    private final String email;
    private final String password;
    private final String nickname;
    private final boolean enabled;
    private final String loginType;
    private final Collection<? extends GrantedAuthority> authorities;

    // OAuth2User  사용자 정보 맵
    private final Map<String, Object> attributes;

    public CustomUserDetails(Long userId, String email, String password, String nickname, boolean enabled, String loginType, Collection<? extends GrantedAuthority> authorities, Map<String, Object> attributes) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.enabled = enabled;
        this.loginType = loginType;
        this.authorities = authorities;
        this.attributes = attributes;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    // OAuth2User 메서드
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    @Override
    public String getName() {
        return email;
    }

}