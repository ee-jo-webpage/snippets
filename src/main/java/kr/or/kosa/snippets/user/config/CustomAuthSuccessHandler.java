package kr.or.kosa.snippets.user.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.or.kosa.snippets.user.loginLog.LoginAttemptService;
import kr.or.kosa.snippets.user.loginLog.LoginLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final LoginLogService loginLogService;
    private final LoginAttemptService loginAttemptService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        System.out.println("로그인 성공!");
        System.out.println("이메일: " + authentication.getName());
        loginLogService.logLogin(authentication.getName(), request, true);
        loginAttemptService.reset(request.getRemoteAddr()); // 차단 초기화
        response.sendRedirect("/");
    }
}