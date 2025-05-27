package kr.or.kosa.snippets.user.config;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.or.kosa.snippets.user.blockIp.IpBlockException;
import kr.or.kosa.snippets.user.loginLog.LoginAttemptService;
import kr.or.kosa.snippets.user.loginLog.LoginLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 로그인 실패 시 호출되는 핸들러
 * - 로그인 실패 로그 기록
 * - 시도 횟수 감지 및 IP 차단 처리
 * - 예외에 따른 사용자 친화적인 에러 메시지 제공
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthFailureHandler implements AuthenticationFailureHandler {

    private final LoginAttemptService loginAttemptService;
    private final LoginLogService loginLogService;
    /**
     * 인증 실패 시 실행되는 메서드
     * - 로그인 실패 기록을 남기고, 로그인 시도 횟수를 누적
     * - 예외 종류에 따라 맞춤 에러 메시지 설정
     * - 로그인 페이지로 리다이렉트 + 에러 메시지 전달
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {

        String email = request.getParameter("username");
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        // 실패 기록 로그 및 시도 횟수 기록
        loginLogService.logLogin(email, request, false);
        loginAttemptService.recordFailure(ip, userAgent);
        // 기본 메세지
        String errorMessage = "로그인에 실패했습니다.";
        // 예외 유형에 따른 에러 메시지 분기
        if (exception instanceof BadCredentialsException) {
            errorMessage = "이메일 또는 비밀번호가 올바르지 않습니다.";
        } else if (exception instanceof DisabledException) {
            errorMessage = "계정이 비활성화되었습니다. 이메일 인증을 완료해주세요.";
        } else if (exception instanceof LockedException) {
            errorMessage = "계정이 잠겨 있습니다.";
        }else if (exception instanceof IpBlockException) {
            errorMessage = "보안 사유로 해당 IP에서의 접속이 차단되었습니다.";
        }


        log.warn("로그인 실패 - 이메일: {}, IP: {}, 메시지: {}", email, ip, errorMessage);
        // 메시지를 인코딩하여 로그인 페이지로 리다이렉트
        response.sendRedirect("/login?error=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8));
    }
}