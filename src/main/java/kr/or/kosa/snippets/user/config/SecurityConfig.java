package kr.or.kosa.snippets.user.config;

import kr.or.kosa.snippets.user.loginLog.IpBlockFilter;
import kr.or.kosa.snippets.user.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomAuthFailureHandler customAuthFailureHandler;
    private final CustomAuthSuccessHandler customAuthSuccessHandler;

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final IpBlockFilter ipBlockFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/community/upload-image"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/user/css/**",
                                "/user/js/**",
                                "/user/images/**"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN", "OAUTH2")
                        .requestMatchers("/loginproc").hasRole("USER") // ROLE_OAUTH2 막기
                        .requestMatchers("/changePassword").hasRole("USER") // ROLE_OAUTH2 막기
                        /**
                         * 인증된 사용자 접근 차단
                         * */
                        .requestMatchers("/login", "/api/register", "/api/verify-code", "/api/forgot-password").anonymous()
                        .requestMatchers("/api/board/**", "/api/community/**", "/community/upload-image", "/community/debug/**").permitAll()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(ipBlockFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(e -> e
                        .accessDeniedHandler(customAccessDeniedHandler))
                /**
                 * 세션로그인 , 로그인 페이지 변경 , 성공,실패 핸들러
                 * */
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/loginproc")
                        .successHandler(customAuthSuccessHandler)
                        .failureHandler(customAuthFailureHandler)
                        .permitAll()
                )

                /**
                 * 소셜 로그인
                 * */
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(customAuthSuccessHandler)
                )
                /**
                 * 로그아웃 시 쿠키 삭제
                 * */
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );



        return http.build();
    }
}