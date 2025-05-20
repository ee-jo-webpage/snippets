package kr.or.kosa.snippets.like;

// SnippetsApplication.java 디버깅 추가

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@MapperScan("kr.or.kosa.snippets.like.mapper")
public class SnippetsApplication {

    public static void main(String[] args) {
        System.out.println("=== Spring Boot 애플리케이션 시작 ===");
        SpringApplication.run(SnippetsApplication.class, args);
    }
}

