package kr.or.kosa.snippets.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    // 구글 확장프로그램용 고정 사용자 ID (코딩하는사람)
    private static final Integer FIXED_USER_ID = 2;

    public static Integer getFixedUserId() {
        return FIXED_USER_ID;
    }
}