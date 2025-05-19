package kr.or.kosa.snippets.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOriginPatterns("*") // 모든 출처 허용
            .allowedMethods("*")        // 모든 HTTP 메서드 허용
            .allowedHeaders("*");       // 모든 헤더 허용
        // allowCredentials 생략 (사용하지 않으므로)
    }
}
