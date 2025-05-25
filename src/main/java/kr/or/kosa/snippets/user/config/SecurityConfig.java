package kr.or.kosa.snippets.user.config;

import kr.or.kosa.snippets.user.loginLog.IpBlockFilter;
import kr.or.kosa.snippets.user.service.CustomOAuth2UserService;
import kr.or.kosa.snippets.user.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomAuthFailureHandler customAuthFailureHandler;
    private final CustomAuthSuccessHandler customAuthSuccessHandler;

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final IpBlockFilter  ipBlockFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
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
                        .requestMatchers("/login", "/api/register", "/api/verify-code", "/api/forgot-password").anonymous()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(ipBlockFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(e -> e
                        .accessDeniedHandler(customAccessDeniedHandler))
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/loginproc")
                        .successHandler(customAuthSuccessHandler)
                        .failureHandler(customAuthFailureHandler)
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(customAuthSuccessHandler)
                )

                .logout(logout -> logout.logoutSuccessUrl("/"));

        return http.build();
    }
}