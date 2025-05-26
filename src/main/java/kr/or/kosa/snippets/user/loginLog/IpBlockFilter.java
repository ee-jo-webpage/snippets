package kr.or.kosa.snippets.user.loginLog;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import kr.or.kosa.snippets.user.blockIp.IpBlockException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class IpBlockFilter extends GenericFilterBean {
    private final LoginAttemptService loginAttemptService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        String ip = req.getRemoteAddr();

        // 여기 로그 넣기 (조건문 바로 위)
        log.info("IP: {}, URI: {}, method: {}", ip, req.getRequestURI(), req.getMethod());
        log.info("차단 여부: {}", loginAttemptService.isBlocked(ip));

        if ("/loginproc".equals(req.getRequestURI())
                && "POST".equalsIgnoreCase(req.getMethod())
                && loginAttemptService.isBlocked(ip)) {

            log.warn("차단된 IP의 로그인 시도: {}", ip);
            HttpServletResponse res = (HttpServletResponse) response;

            String errorMessage = "보안 사유로 해당 IP에서의 접속이 차단되었습니다.";
            String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
            res.sendRedirect("/login?error=" + encodedMessage);
            return;
        }

        chain.doFilter(request, response);
    }

}