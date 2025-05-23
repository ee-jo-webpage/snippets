package kr.or.kosa.snippets.user.config;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.or.kosa.snippets.user.loginLog.LoginLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthFailureHandler implements AuthenticationFailureHandler {



    private final LoginLogService loginLogService;
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {
        String email = request.getParameter("username"); // 로그인 시도한 이메일
        loginLogService.logLogin(email, request, false);
        log.warn("로그인 실패: {}", exception.getMessage());
        if (exception instanceof BadCredentialsException) {
            log.warn("비밀번호가 틀렸습니다.");
        }

        response.sendRedirect("/login?error");
    }
}