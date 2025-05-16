package kr.or.kosa.snippets.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class SessionInitInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        HttpSession session = request.getSession();

        // 세션에 userId가 없으면 임시 사용자 ID 설정
        if (session.getAttribute("userId") == null) {
            session.setAttribute("userId", 1L);  // 기본 사용자 ID
            log.info("세션 자동 초기화 - 임시 사용자 ID: 1");
        }

        return true;
    }
}