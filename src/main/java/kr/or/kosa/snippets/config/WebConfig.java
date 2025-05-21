package kr.or.kosa.snippets.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String CHROME_EXTENSION_ID = "chrome-extension://igpcgihogdhcefmbfhcejhdaohpnfaee";

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            // .allowedOrigins("http://localhost:8090", CHROME_EXTENSION_ID)
            .allowedOrigins("*")
            .allowedMethods("*")
            .allowedHeaders("*");
            // .allowCredentials(true);
    }
}